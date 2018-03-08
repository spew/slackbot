package org.poker.stock;

import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;

public class YahooFinanceStockResolver {
    public Stock resolve(String ticker) {
        try {
            return YahooFinance.get(sanitizeTicker(ticker));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String sanitizeTicker(String ticker) {
        if (ticker.equalsIgnoreCase("pcln")) {
            ticker = "bkng";
        }
        if (ticker.startsWith(".")) {
            ticker = "^" + ticker.substring(1);
        }
        return ticker.replace('.', '-');
    }
}
