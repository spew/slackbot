package org.poker.coronavirus;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class VirusStatsRetriever {
    private final String url = "https://www.worldometers.info/coronavirus/";

    public VirusStatsRetriever() {

    }

    public VirusStats retrieve() {
        try {
            return retrieveThrows();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private VirusStats retrieveThrows() throws IOException {
        Document document = Jsoup.connect(url).get();
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

    private int getIntValue(String value) {
        NumberFormat format = NumberFormat.getInstance(Locale.US);
        try {
            Number number = format.parse(value);
            return number.intValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
