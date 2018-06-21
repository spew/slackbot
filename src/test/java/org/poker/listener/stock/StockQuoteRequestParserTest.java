package org.poker.listener.stock;

import org.junit.Test;
import org.poker.stock.StockQuoteRequestParser;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class StockQuoteRequestParserTest {
    @Test
    public void emptyString() {
        StockQuoteRequestParser parser = new StockQuoteRequestParser();
        List<String> tickers = parser.getTickers("");
        assertThat(tickers, is(Arrays.asList()));
    }

    @Test
    public void singleQuote() {
        StockQuoteRequestParser parser = new StockQuoteRequestParser();
        List<String> tickers = parser.getTickers("$amzn");
        assertThat(tickers, is(Arrays.asList("amzn")));
    }

    @Test
    public void singleQuoteWithWhitespace() {
        StockQuoteRequestParser parser = new StockQuoteRequestParser();
        List<String> tickers = parser.getTickers(" $amzn    ");
        assertThat(tickers, is(Arrays.asList("amzn")));
    }

    @Test
    public void multipleTickers() {
        StockQuoteRequestParser parser = new StockQuoteRequestParser();
        List<String> tickers = parser.getTickers("$goog $msft $brk.a $.dji");
        assertThat(tickers, is(Arrays.asList("goog", "msft", "brk.a", ".dji")));
    }

    @Test
    public void multipleTickersInSentence() {
        StockQuoteRequestParser parser = new StockQuoteRequestParser();
        List<String> tickers = parser.getTickers("The quick brown fox $goog $msft jumps over $brk.a the $.dji lazy dog");
        assertThat(tickers, is(Arrays.asList("goog", "msft", "brk.a", ".dji")));
    }
}
