package org.poker.stock;

import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.*;

public class YahooFinanceStockResolver {
    public List<ExtendedStockQuote> resolve(String ... tickers) {
        String sanitizedTickers = sanitizeTickers(tickers);
        try {
            // this ugliness exists to extract the pre / post market data.
            ExtendedHoursStockQuotesQuery1V7Request request = new ExtendedHoursStockQuotesQuery1V7Request(sanitizedTickers);
            return request.getResult();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String sanitizeTickers(String ... tickers) {
        List<String> results = new ArrayList<>();
        for (String t : tickers) {
            results.add(sanitizeTicker(t));
        }
        return Joiner.on(",").join(results);

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
