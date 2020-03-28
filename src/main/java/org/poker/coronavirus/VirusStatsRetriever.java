package org.poker.coronavirus;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class VirusStatsRetriever {
    private final String url = "https://www.worldometers.info/coronavirus/";

    public VirusStatsRetriever() {

    }

    public VirusStats retrieve() {
        return retrieve(null);
    }

    public VirusStats retrieve(String country) {
        Document document = getDocument(country);
        String selector = "#maincounter-wrap > div > span";
        Elements elements = document.select(selector);
        int expectedCount = 3;
        if (elements.size() != expectedCount) {
            throw new RuntimeException(String.format("Unexpected number of elements found for selector '%s': got '%v', want '%v'",
                    selector, elements.size(), expectedCount));
        }
        VirusStats stats = VirusStats.newBuilder()
                .withTotal(getIntValue(elements.get(0).text()))
                .withDeaths(getIntValue(elements.get(1).text()))
                .withRecoveries(getIntValue(elements.get(2).text()))
                .build();
        return stats;
    }

    public VirusStats retrieveUSA() {
        return retrieve("US");
    }

    private String getUrl(String country) {
        if (country == null || country.isEmpty()) {
            return url;
        }
        return String.format("%s/country/%s", url, country);
    }

    private int getIntValue(String value) {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        try {
            Number number = format.parse(value);
            return number.intValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Document getDocument(String country) {
        long initialInterval = TimeUnit.SECONDS.toMillis(1);
        IntervalFunction intervalFn =
                IntervalFunction.ofExponentialBackoff(initialInterval);
        RetryConfig retryConfig = RetryConfig.custom()
                .maxAttempts(5)
                .intervalFunction(intervalFn)
                .build();
        Retry retry = Retry.of("coronavirus-stats", retryConfig);
        Function<String, Document> getDocumentFunc = Retry
                .decorateFunction(retry, c -> getDocumentThrows(c));
        return getDocumentFunc.apply(country);
    }

    private Document getDocumentThrows(String country) {
        String url = getUrl(country);
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
