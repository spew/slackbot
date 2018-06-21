package org.poker.stock;

import java.util.ArrayList;
import java.util.List;

public class StockQuoteRequestParser {
    public List<String> getTickers(String message) {
        List<String> results = new ArrayList<>();
        String[] tokens = message.split(" ");
        for (String t : tokens) {
            if (t.startsWith("$") && t.length() <= 10) {
                results.add(t.substring(1).trim());
            }
        }
        return results;
    }
}
