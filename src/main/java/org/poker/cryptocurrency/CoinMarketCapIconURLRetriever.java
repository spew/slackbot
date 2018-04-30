package org.poker.cryptocurrency;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Optional;

public class CoinMarketCapIconURLRetriever {
    public Optional<String> retrieve(String coinName) {
        try {
            Document document = Jsoup.connect(formatUrl(coinName)).get();
            Element elem = document.selectFirst("meta[property=og:image]");
            if (elem == null) {
                return Optional.empty();
            }
            return Optional.of(elem.attributes().get("content"));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private String formatUrl(String coinName) {
        return String.format("https://coinmarketcap.com/currencies/%s/", coinName);
    }
}
