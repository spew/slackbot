package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import humanize.ICUHumanize;
import org.poker.stock.ExtendedStockQuote;
import org.poker.stock.LogoURLRetriever;
import org.poker.stock.StockQuoteRequestParser;
import org.poker.stock.YahooFinanceStockResolver;
import yahoofinance.Stock;
import yahoofinance.quotes.stock.ExtendedHoursStockQuote;
import yahoofinance.quotes.stock.StockQuote;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StockQuoteMessageListener implements SlackMessagePostedListener {
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private final YahooFinanceStockResolver stockResolver;
    private final LogoURLRetriever logoURLRetriever;

    public StockQuoteMessageListener(YahooFinanceStockResolver stockResolver, LogoURLRetriever logoURLRetriever) {
        this.stockResolver = stockResolver;
        this.logoURLRetriever = logoURLRetriever;
    }

    public void onEvent(SlackMessagePosted event, SlackSession session) {
        String message = event.getMessageContent();
        List<String> tickers = new StockQuoteRequestParser().getTickers(message);
        if (tickers.isEmpty()) {
            return;
        }
        List<SlackAttachment> attachments = new ArrayList<>();
        List<ExtendedStockQuote> quotes = stockResolver.resolve(tickers.toArray(new String[tickers.size()]));
        for (ExtendedStockQuote q : quotes) {
            if (q.getStock().isValid()) {
                StockQuote quote = q.getStock().getQuote();
                attachments.add(formatAttachment(q, quote));
            }
        }
        if (attachments.isEmpty()) {
            return;
        }
        SlackPreparedMessage preparedMessage = new SlackPreparedMessage.Builder()
                .withAttachments(attachments)
                .build();
        session.sendMessage(event.getChannel(), preparedMessage);
    }

    private SlackAttachment formatAttachment(ExtendedStockQuote extStockQuote, StockQuote quote) {
        Stock stock = extStockQuote.getStock();
        SlackAttachment attachment = new SlackAttachment();
        attachment.setFallback("");
        BigDecimal percentChange;
        if (shouldAddExtendedHoursMessage(extStockQuote.getExtendedHoursStockQuote())) {
            percentChange = extStockQuote.getExtendedHoursStockQuote().getChangePercent();
        } else {
            percentChange = quote.getChangeInPercent();
        }
        attachment.setColor(getColor(percentChange));
        //TODO: find better place for images to support moar exchanges
        // attachment.setThumbUrl("https://www.nasdaq.com/logos/" + stock.getSymbol().toUpperCase() + ".GIF");
        Optional<String> thumbUrl = logoURLRetriever.retrieve(stock.getName());
        thumbUrl.ifPresent(attachment::setThumbUrl);
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
            if (percentChange24Hour.compareTo(new BigDecimal(-5, MathContext.DECIMAL32)) >= 0) {
                return "warning";
            }
            return "danger";
        }
    }
}
