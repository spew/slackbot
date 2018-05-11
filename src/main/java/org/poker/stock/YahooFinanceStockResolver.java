package org.poker.stock;

import yahoofinance.YahooFinance;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YahooFinanceStockResolver {
    public ExtendedStockQuote resolve(String ticker) {
        ticker = sanitizeTicker(ticker);
        try {
            // this ugliness exists to extract the pre / post market data.
            Map<String, ExtendedStockQuote> result = new HashMap<>();
            ExtendedHoursStockQuotesQuery1V7Request request = new ExtendedHoursStockQuotesQuery1V7Request(ticker);
            List<ExtendedStockQuote> stocks = request.getResult();
            for(ExtendedStockQuote extStock : stocks) {
                result.put(extStock.getStock().getSymbol(), extStock);
            }
            return result.get(ticker.toUpperCase());
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
