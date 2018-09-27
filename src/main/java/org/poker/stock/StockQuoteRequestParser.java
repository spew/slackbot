package org.poker.stock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StockQuoteRequestParser {
    private static final String FAANG_TICKER = "FAANG";
    private static final String FAANGS_TICKER = "FAANGS";
    private static final List<String> FAANG_TICKER_LIST = Arrays.asList("FB", "AMZN", "AAPL", "NFLX", "GOOG");
    private static final List<String> FAANGS_TICKER_LIST = Stream.concat(FAANG_TICKER_LIST.stream(), Arrays.asList("SQ").stream()).collect(Collectors.toList());

    public List<String> getTickers(String message) {
        List<String> results = new ArrayList<>();
        String[] tokens = message.split(" ");
        for (String t : tokens) {
            if (t.startsWith("$") && t.length() <= 10) {
                String ticker = t.substring(1).trim();
                results.addAll(expandTicker(ticker));
            }
        }
        return results;
    }

    private List<String> expandTicker(String ticker) {
        switch (ticker.toUpperCase()) {
            case FAANG_TICKER:
                return FAANG_TICKER_LIST;
            case FAANGS_TICKER:
                return FAANGS_TICKER_LIST;
            default:
                return Arrays.asList(ticker);
        }
    }
}
