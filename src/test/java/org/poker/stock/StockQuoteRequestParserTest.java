package org.poker.stock;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StockQuoteRequestParserTest {
    @Test
    public void basic() {
        List<String> tickers = new StockQuoteRequestParser().getTickers("$GOOG");
        assertEquals(Arrays.asList("GOOG"), tickers);
    }

    @Test
    public void faang() {
        List<String> tickers = new StockQuoteRequestParser().getTickers("$FAANG");
        assertEquals(Arrays.asList("FB", "AMZN", "AAPL", "NFLX", "GOOG"), tickers);
    }
}
