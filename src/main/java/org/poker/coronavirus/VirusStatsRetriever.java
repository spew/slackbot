package org.poker.coronavirus;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Element;

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
        //String selector = "#maincounter-wrap > div > span";
        Element totalRow = findTotalsRow(document);
        Elements elements = totalRow.select("td");
        VirusStats stats = VirusStats.newBuilder()
                .withTotalCases(getIntValue(elements.get(2).text()))
                .withTotalCasesDelta(getIntValue(elements.get(3).text()))
                .withDeaths(getIntValue(elements.get(4).text()))
                .withDeathsDelta(getIntValue(elements.get(5).text()))
                .withRecoveries(getIntValue(elements.get(6).text()))
                .build();
        return stats;
    }

    private Element findTotalsRow(Document document) {
        String selector = "#nav-today > div > table > tbody > tr";
        Elements elements = document.select(selector);
        Element totalRow = findShortestNamedTotalRow(elements);
        if (totalRow == null) {
            throw new RuntimeException("expected to find a total row in document");
        }
        return totalRow;
    }

    private Element findShortestNamedTotalRow(Elements elements) {
        Element minTotal = null;
        for (Element e : elements) {
            if (!isTotalRow(e)) {
                continue;
            }
            if (minTotal == null) {
                minTotal = e;
            } else {
                if (e.className().length() < minTotal.className().length()) {
                    minTotal = e;
                }
            }
        }
        return minTotal;
    }

    private boolean isTotalRow(Element e) {
        return e.is("tr") && e.className().contains("total_row");
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
        if (value.equals("")) {
            return 0;
        }
        if (value.startsWith("+")) {
            value = value.substring("+".length());
        }
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
