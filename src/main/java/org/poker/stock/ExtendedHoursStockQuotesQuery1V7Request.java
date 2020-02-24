package org.poker.stock;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import yahoofinance.Stock;
import yahoofinance.Utils;
import yahoofinance.exchanges.ExchangeTimeZone;
import yahoofinance.quotes.query1v7.QuotesRequest;
import yahoofinance.quotes.stock.*;
import yahoofinance.quotes.stock.ExtendedHoursStockQuote;
import yahoofinance.quotes.stock.ExtendedHoursStockQuoteType;

import java.math.BigDecimal;
import java.util.TimeZone;

/*
 * hack to enable retrieving pre & post market data
 * not thread safe
 */
public class ExtendedHoursStockQuotesQuery1V7Request extends QuotesRequest<ExtendedStockQuote> {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    public ExtendedHoursStockQuotesQuery1V7Request(String symbols) {
        super(symbols);
    }

    @Override
    protected ExtendedStockQuote parseJson(JsonNode node) {
        Stock stock = parseStock(node);
        ExtendedHoursStockQuote extendedHoursStockQuote = getExtendedHoursQuote(node);
        return new ExtendedStockQuote(stock, extendedHoursStockQuote);
    }

    private Stock parseStock(JsonNode node) {
        String symbol = node.get("symbol").asText();
        Stock stock = new Stock(symbol);

        if(node.has("longName")) {
            stock.setName(node.get("longName").asText());
        } else {
            stock.setName(getStringValue(node, "shortName"));
        }

        stock.setCurrency(getStringValue(node, "currency"));
        stock.setStockExchange(getStringValue(node, "fullExchangeName"));

        stock.setQuote(this.getQuote(node));
        stock.setStats(this.getStats(node));
        stock.setDividend(this.getDividend(node));

        return stock;
    }

    private String getStringValue(JsonNode node, String field) {
        if(node.has(field)) {
            return node.get(field).asText();
        }
        return null;
    }

    private String getExtendedHoursPrefix(JsonNode node) {
        String marketState = StringUtils.trimToEmpty(getStringValue(node, "marketState"));

        if (marketState.equals("PRE")) {
            return "pre";
        }
        if (marketState.equals("POST")) {
            return "post";
        }
        if (marketState.equals("CLOSED")) {
            return "post";
        }
        return null;
    }

    private ExtendedHoursStockQuote getExtendedHoursQuote(JsonNode node) {
        String prefix = getExtendedHoursPrefix(node);
        if (prefix == null) {
            return null;
        }
        ExtendedHoursStockQuote extendedHoursStockQuote = new ExtendedHoursStockQuote();
        extendedHoursStockQuote.setChangePercent(Utils.getBigDecimal(getStringValue(node, prefix + "MarketChangePercent")));
        String marketTime = getStringValue(node, prefix + "MarketTime");
        if (marketTime != null) {
            extendedHoursStockQuote.setTime(Utils.unixToCalendar(Utils.getLong(marketTime)));
        }
        extendedHoursStockQuote.setPrice(Utils.getBigDecimal(getStringValue(node, prefix + "MarketPrice")));
        extendedHoursStockQuote.setPriceChange(Utils.getBigDecimal(getStringValue(node,prefix + "MarketChange")));
        extendedHoursStockQuote.setType(ExtendedHoursStockQuoteType.valueOf(getStringValue(node, "marketState")));
        return extendedHoursStockQuote;
    }

    private StockQuote getQuote(JsonNode node) {
        String symbol = node.get("symbol").asText();
        StockQuote quote = new StockQuote(symbol);

        quote.setPrice(Utils.getBigDecimal(getStringValue(node,"regularMarketPrice")));
        // quote.setLastTradeSize(null);
        quote.setAsk(Utils.getBigDecimal(getStringValue(node,"ask")));
        quote.setAskSize(Utils.getLong(getStringValue(node,"askSize")));
        quote.setBid(Utils.getBigDecimal(getStringValue(node,"bid")));
        quote.setBidSize(Utils.getLong(getStringValue(node,"bidSize")));
        quote.setOpen(Utils.getBigDecimal(getStringValue(node,"regularMarketOpen")));
        quote.setPreviousClose(Utils.getBigDecimal(getStringValue(node,"regularMarketPreviousClose")));
        quote.setDayHigh(Utils.getBigDecimal(getStringValue(node,"regularMarketDayHigh")));
        quote.setDayLow(Utils.getBigDecimal(getStringValue(node,"regularMarketDayLow")));

        if(node.has("exchangeTimezoneName")) {
            quote.setTimeZone(TimeZone.getTimeZone(node.get("exchangeTimezoneName").asText()));
        } else {
            quote.setTimeZone(ExchangeTimeZone.getStockTimeZone(symbol));
        }

        if(node.has("regularMarketTime")) {
            quote.setLastTradeTime(Utils.unixToCalendar(node.get("regularMarketTime").asLong()));
        }

        quote.setYearHigh(Utils.getBigDecimal(getStringValue(node,"fiftyTwoWeekHigh")));
        quote.setYearLow(Utils.getBigDecimal(getStringValue(node,"fiftyTwoWeekLow")));
        quote.setPriceAvg50(Utils.getBigDecimal(getStringValue(node,"fiftyDayAverage")));
        quote.setPriceAvg200(Utils.getBigDecimal(getStringValue(node,"twoHundredDayAverage")));

        quote.setVolume(Utils.getLong(getStringValue(node,"regularMarketVolume")));
        quote.setAvgVolume(Utils.getLong(getStringValue(node,"averageDailyVolume3Month")));

        return quote;
    }

    private StockStats getStats(JsonNode node) {
        String symbol = getStringValue(node,"symbol");
        StockStats stats = new StockStats(symbol);

        stats.setMarketCap(Utils.getBigDecimal(getStringValue(node,"marketCap")));
        // stats.setSharesFloat(Utils.getLong(getStringValue(node,"sharesOutstanding")));
        stats.setSharesOutstanding(Utils.getLong(getStringValue(node,"sharesOutstanding")));
        // stats.setSharesOwned(Utils.getLong(getStringValue(node,"symbol")));

        stats.setEps(Utils.getBigDecimal(getStringValue(node,"epsTrailingTwelveMonths")));
        stats.setPe(Utils.getBigDecimal(getStringValue(node,"trailingPE")));
        // stats.setPeg(Utils.getBigDecimal(getStringValue(node,"symbol")));

        stats.setEpsEstimateCurrentYear(Utils.getBigDecimal(getStringValue(node,"epsForward")));
        // stats.setEpsEstimateNextQuarter(Utils.getBigDecimal(getStringValue(node,"symbol")));
        // stats.setEpsEstimateNextYear(Utils.getBigDecimal(getStringValue(node,"symbol")));

        stats.setPriceBook(Utils.getBigDecimal(getStringValue(node,"priceToBook")));
        // stats.setPriceSales(Utils.getBigDecimal(getStringValue(node,"symbol")));
        stats.setBookValuePerShare(Utils.getBigDecimal(getStringValue(node,"bookValue")));

        // stats.setOneYearTargetPrice(Utils.getBigDecimal(getStringValue(node,"symbol")));
        // stats.setEBITDA(Utils.getBigDecimal(getStringValue(node,"symbol")));
        // stats.setRevenue(Utils.getBigDecimal(getStringValue(node,"symbol")));

        // stats.setShortRatio(Utils.getBigDecimal(getStringValue(node,"symbol")));

        if(node.has("earningsTimestamp")) {
            stats.setEarningsAnnouncement(Utils.unixToCalendar(node.get("earningsTimestamp").asLong()));
        }

        return stats;
    }

    private StockDividend getDividend(JsonNode node) {
        String symbol = this.getStringValue(node, "symbol");
        StockDividend dividend = new StockDividend(symbol);

        if (node.has("dividendDate")) {
            long dividendTimestamp = node.get("dividendDate").asLong();
            dividend.setPayDate(Utils.unixToCalendar(dividendTimestamp));
            // dividend.setExDate(Utils.unixToCalendar(node.get("dividendDate").asLong()));
        }
        if (node.has("trailingAnnualDividendRate")) {
            dividend.setAnnualYield(Utils.getBigDecimal(this.getStringValue(node, "trailingAnnualDividendRate")));
        }
        if (node.has("trailingAnnualDividendYield")) {
            BigDecimal yield = Utils.getBigDecimal(this.getStringValue(node, "trailingAnnualDividendYield"));
            if (yield != null) {
                dividend.setAnnualYieldPercent(yield.multiply(ONE_HUNDRED));
            }
        }

        return dividend;
    }

}
