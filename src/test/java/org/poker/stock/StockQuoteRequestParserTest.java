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
    public void FAANG() {
        List<String> tickers = new StockQuoteRequestParser().getTickers("$FAANG");
        assertEquals(Arrays.asList("FB", "AMZN", "AAPL", "NFLX", "GOOG"), tickers);
    }

    @Test
    public void fAanG() {
        List<String> tickers = new StockQuoteRequestParser().getTickers("$fAanG");
        assertEquals(Arrays.asList("FB", "AMZN", "AAPL", "NFLX", "GOOG"), tickers);
    }

    @Test
    public void FAANGS() {
        List<String> tickers = new StockQuoteRequestParser().getTickers("$FAANGS");
        assertEquals(Arrays.asList("FB", "AMZN", "AAPL", "NFLX", "GOOG", "SQ"), tickers);
    }

    @Test
    public void MAGA() {
        List<String> tickers = new StockQuoteRequestParser().getTickers("$MAGA");
        assertEquals(Arrays.asList("MSFT", "AAPL", "GOOG", "AMZN"), tickers);
    }

    @Test
    public void FOOD() {
        List<String> tickers = new StockQuoteRequestParser().getTickers("$FOOD");
        assertEquals(Arrays.asList("CMG", "DPZ", "MCD", "NDLS", "YUM"), tickers);
    }

    @Test
    public void VEGAS() {
        List<String> tickers = new StockQuoteRequestParser().getTickers("$VEGAS");
        assertEquals(Arrays.asList("LVS", "MGM", "WYNN"), tickers);
    }
}
