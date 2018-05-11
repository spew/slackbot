package org.poker.stock;

import yahoofinance.Stock;
import yahoofinance.quotes.stock.ExtendedHoursStockQuote;
import yahoofinance.quotes.stock.StockQuote;

public class ExtendedStockQuote {
    private final Stock stock;
    private final ExtendedHoursStockQuote extendedHoursStockQuote;

    public ExtendedStockQuote(Stock stock, ExtendedHoursStockQuote extendedHoursStockQuote) {
        this.stock = stock;
        this.extendedHoursStockQuote = extendedHoursStockQuote;
    }

    public Stock getStock() {
        return stock;
    }

    public ExtendedHoursStockQuote getExtendedHoursStockQuote() {
        return extendedHoursStockQuote;
    }
}
