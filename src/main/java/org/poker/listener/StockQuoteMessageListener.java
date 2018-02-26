package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import humanize.ICUHumanize;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.quotes.stock.StockQuote;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.Calendar;

public class StockQuoteMessageListener implements SlackMessagePostedListener {
  private static final DecimalFormat decimalFormat = new DecimalFormat("#,###.00");;

  public StockQuoteMessageListener() {
  }

  public void onEvent(SlackMessagePosted event, SlackSession session) {
    String message = event.getMessageContent();
    if (!message.startsWith("$") || message.length() > 10) {
      return;
    }
    String coin = getCurrencyCodeFromMessage(message);
    SlackChannel channel = event.getChannel();

    try {
      Stock stock = YahooFinance.get(coin);
      if (stock.isValid()) {
        StockQuote quote = stock.getQuote();
        SlackAttachment attachment = formatAttachment(stock, quote);
        session.sendMessage(channel, attachment.getFallback(), attachment);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }


  private String getCurrencyCodeFromMessage(String message) {
    String ticker = message.substring(1);
    if (ticker.startsWith(".")) {
      ticker = "^" + ticker.substring(1);
    }
    return ticker.replace('.', '-');
  }

  private SlackAttachment formatAttachment(Stock stock, StockQuote quote) {
    SlackAttachment attachment = new SlackAttachment();
    attachment.setFallback("");
    attachment.setColor(getColor(quote.getChange()));
    //TODO: find better place for images to support moar exchanges
    attachment.setThumbUrl("https://www.nasdaq.com/logos/" + stock.getSymbol().toUpperCase() + ".GIF");
    attachment.addField(stock.getName(), formatMessage(stock, quote), false);
    return attachment;
  }

  private String formatMessage(Stock stock, StockQuote quote) {
    BigDecimal usdPrice = quote.getPrice();
    BigDecimal percentChange24Hour = quote.getChangeInPercent();
    boolean isZeroOrPositive = percentChange24Hour.compareTo(BigDecimal.ZERO) >= 0;
    StringBuilder sb = new StringBuilder();
    sb.append(String.format("$%s %s%s (%s%%)",
        decimalFormat.format(usdPrice),
        isZeroOrPositive ? "+" : "",
        decimalFormat.format(quote.getChange()),
        decimalFormat.format(percentChange24Hour)));
    if (stock.getStats().getMarketCap() != null) {
      sb.append(" | Cap: ");
      sb.append("$");
      sb.append(ICUHumanize.compactDecimal(stock.getStats().getMarketCap()));
    }
    sb.append(" | Vol: ");
    sb.append(ICUHumanize.compactDecimal(quote.getVolume()));
    return sb.toString();
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
