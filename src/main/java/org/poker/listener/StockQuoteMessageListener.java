package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import humanize.ICUHumanize;
import org.poker.stock.ExtendedStockQuote;
import org.poker.stock.StockQuoteRequestParser;
import org.poker.stock.YahooFinanceStockResolver;
import yahoofinance.Stock;
import yahoofinance.quotes.stock.ExtendedHoursStockQuote;
import yahoofinance.quotes.stock.StockQuote;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.List;

public class StockQuoteMessageListener implements SlackMessagePostedListener {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private final YahooFinanceStockResolver stockResolver;

    public StockQuoteMessageListener(YahooFinanceStockResolver stockResolver) {
        this.stockResolver = stockResolver;
    }

    public void onEvent(SlackMessagePosted event, SlackSession session) {
        String message = event.getMessageContent();
        List<String> tickers = new StockQuoteRequestParser().getTickers(message);
        if (tickers.isEmpty()) {
            return;
        }
        SlackChannel channel = event.getChannel();
        ExtendedStockQuote extStockQuote = stockResolver.resolve(tickers.get(0));
        if (extStockQuote.getStock().isValid()) {
            StockQuote quote = extStockQuote.getStock().getQuote();
            SlackAttachment attachment = formatAttachment(extStockQuote, quote);
            session.sendMessage(channel, attachment.getFallback(), attachment);
        }
    }

    private SlackAttachment formatAttachment(ExtendedStockQuote extStockQuote, StockQuote quote) {
        Stock stock = extStockQuote.getStock();
        SlackAttachment attachment = new SlackAttachment();
        attachment.setFallback("");
        attachment.setColor(getColor(quote.getChange()));
        //TODO: find better place for images to support moar exchanges
        attachment.setThumbUrl("https://www.nasdaq.com/logos/" + stock.getSymbol().toUpperCase() + ".GIF");
        attachment.addField(stock.getName(), formatMessage(extStockQuote), false);
        return attachment;
    }

    private String formatMessage(ExtendedStockQuote extendedStockQuote) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatMainMessage(extendedStockQuote.getStock()));
        if (shouldAddExtendedHoursMessage(extendedStockQuote.getExtendedHoursStockQuote())) {
            sb.append("\n");
            sb.append(formatExtendedHoursMessage(extendedStockQuote.getExtendedHoursStockQuote()));
        }
        return sb.toString();
    }

    private boolean shouldAddExtendedHoursMessage(ExtendedHoursStockQuote extendedHoursStockQuote) {
        return extendedHoursStockQuote != null && extendedHoursStockQuote.getChangePercent() != null && extendedHoursStockQuote.getPrice() != null
                && extendedHoursStockQuote.getPriceChange() != null;
    }

    private String formatExtendedHoursMessage(ExtendedHoursStockQuote extendedHoursStockQuote) {
        return String.format("%s: %s",
                getExtendedHoursPrefix(extendedHoursStockQuote),
                formatPrice(extendedHoursStockQuote.getPrice(), extendedHoursStockQuote.getPriceChange(), extendedHoursStockQuote.getChangePercent()));
    }

    private String getExtendedHoursPrefix(ExtendedHoursStockQuote extendedHoursStockQuote) {
        switch (extendedHoursStockQuote.getType()) {
            case PRE:
                return "Pre-market";
            case POST:
            case CLOSED:
                return "After hours";
        }
        return null;
    }

    private String formatMainMessage(Stock stock) {
        StockQuote quote = stock.getQuote();
        BigDecimal usdPrice = quote.getPrice();
        StringBuilder sb = new StringBuilder();
        sb.append(formatPrice(usdPrice, quote.getChange(), quote.getChangeInPercent()));
        if (stock.getStats().getMarketCap() != null) {
            sb.append(" | Cap: ");
            sb.append("$");
            sb.append(ICUHumanize.compactDecimal(stock.getStats().getMarketCap()));
        }
        sb.append(" | Vol: ");
        sb.append(ICUHumanize.compactDecimal(quote.getVolume()));
        return sb.toString();
    }

    private String formatPrice(BigDecimal usdPrice, BigDecimal changeAmount, BigDecimal percentChange24Hour) {
        boolean isZeroOrPositive = percentChange24Hour.compareTo(BigDecimal.ZERO) >= 0;
        return String.format("$%s %s%s (%s%%)",
                decimalFormat.format(usdPrice),
                isZeroOrPositive ? "+" : "",
                decimalFormat.format(changeAmount),
                decimalFormat.format(percentChange24Hour));
    }

    private String getColor(BigDecimal percentChange24Hour) {
        int cmp = percentChange24Hour.compareTo(BigDecimal.ZERO);
        if (cmp == 0) {
            return "warning";
        } else if (cmp > 0) {
            return "good";
        } else {
            if (percentChange24Hour.compareTo(new BigDecimal(-10, MathContext.DECIMAL32)) >= 0) {
                return "warning";
            }
            return "danger";
        }
    }
}
