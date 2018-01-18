package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import humanize.ICUHumanize;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.binance.service.BinanceMarketDataServiceRaw;
import org.knowm.xchange.coinmarketcap.CoinMarketCapExchange;
import org.knowm.xchange.coinmarketcap.dto.marketdata.CoinMarketCapTicker;
import org.knowm.xchange.coinmarketcap.service.CoinMarketCapMarketDataService;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CryptoCurrencyMessageListener implements SlackMessagePostedListener {
  private List<Exchange> exchanges = new ArrayList<>();
  private static final DecimalFormat decimalFormat = new DecimalFormat("#,###.00");;
  private BinanceExchange binanceExchange;

  public CryptoCurrencyMessageListener() {
    ExchangeSpecification coinMarketCapExchangeSpecification = new CoinMarketCapExchange().getDefaultExchangeSpecification();
    exchanges.add(ExchangeFactory.INSTANCE.createExchange(coinMarketCapExchangeSpecification));
    ExchangeSpecification binanceExchangeSpecification = new BinanceExchange().getDefaultExchangeSpecification();
    binanceExchange = (BinanceExchange)ExchangeFactory.INSTANCE.createExchange(binanceExchangeSpecification);
  }

  public CryptoCurrencyMessageListener(Exchange ... exchanges) {

  }

  public void onEvent(SlackMessagePosted event, SlackSession session) {
    String message = event.getMessageContent();
    if (!message.startsWith(".") || message.length() > 10) {
      return;
    }
    String coin = getCurrencyCodeFromMessage(message);
    SlackChannel channel = event.getChannel();
    SlackUser messageSender = event.getSender();
    for (Exchange e : exchanges) {
      List<CurrencyPair> currencyPairs = e.getExchangeSymbols();
      for (CurrencyPair cp : currencyPairs) {
        if (cp.base.getCurrencyCode().equalsIgnoreCase(coin) && cp.counter.getCurrencyCode().equals("USD")) {
          CoinMarketCapTicker ticker = getCoinMarketCapTicker(e, cp);
          BigDecimal usdPrice = ticker.getPriceUSD();
          BigDecimal percentChange24Hour = ticker.getPctChange24h();
          boolean isZeroOrPositive = percentChange24Hour.compareTo(BigDecimal.ZERO) >= 0;
          SlackAttachment attachment = formatAttachment(cp.base.getCurrencyCode(), ticker);
          session.sendMessage(channel, attachment.getFallback(), attachment);
        }
      }
    }
  }

  private String getCurrencyCodeFromMessage(String message) {
    if (message.equalsIgnoreCase(".ether")) {
      return "ETH";
    }
    return message.substring(1);
  }

  private BinanceTicker24h getBinanceTicker(String baseCurrencyCode, String counterCurrencyCode) {
    BinanceMarketDataServiceRaw dataService = (BinanceMarketDataService)binanceExchange.getMarketDataService();
    CurrencyPair cp = getCurrencyPairForExchange(binanceExchange, baseCurrencyCode, counterCurrencyCode);
    if (cp == null) {
      return null;
    }
    try {
      return dataService.ticker24h(cp);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private CurrencyPair getCurrencyPairForExchange(Exchange e, String baseCurrencyCode, String counterCurrencyCode) {
    List<CurrencyPair> currencyPairs = e.getExchangeSymbols();
    for (CurrencyPair cp : currencyPairs) {
      if (cp.base.getCurrencyCode().equalsIgnoreCase(baseCurrencyCode) && cp.counter.getCurrencyCode().equalsIgnoreCase(counterCurrencyCode)) {
        return cp;
      }
    }
    return null;
  }

  private SlackAttachment formatAttachment(String baseCurrencyCode, CoinMarketCapTicker ticker) {
    BigDecimal usdPrice = ticker.getPriceUSD();
    BigDecimal percentChange24Hour = ticker.getPctChange24h();
    String currencyCode = ticker.getBaseCurrency().getCurrency().getCurrencyCode();
    SlackAttachment attachment = new SlackAttachment();
    boolean isZeroOrPositive = percentChange24Hour.compareTo(BigDecimal.ZERO) >= 0;
    String strMessage = String.format("%s (%s): $%s (%s%s%%)",
        ticker.getName(),
        currencyCode,
        decimalFormat.format(usdPrice),
        isZeroOrPositive ? "+" : "",
        decimalFormat.format(percentChange24Hour));
    attachment.setFallback(strMessage);
    attachment.setColor(getColor(ticker));
    attachment.setThumbUrl("https://files.coinmarketcap.com/static/img/coins/128x128/" + ticker.getID() + ".png");
    String overview = String.format("Cap: %s\nVolume: %s",
        "$" + ICUHumanize.compactDecimal(ticker.getMarketCapUSD()),
        ICUHumanize.compactDecimal(ticker.getVolume24hUSD()));
    attachment.addField("Overview", overview, true);
    attachment.addField("Binance", formatBinancePrices(baseCurrencyCode), true);
    return attachment;
  }

  private String formatBinancePrices(String baseCurrencyCode) {
    BinanceTicker24h usdtTicker = getBinanceTicker(baseCurrencyCode, "USDT");
    BinanceTicker24h btcTicker = getBinanceTicker(baseCurrencyCode, "BTC");
    BinanceTicker24h ethTicker = getBinanceTicker(baseCurrencyCode, "ETH");
    List<String> results = new ArrayList<>();
    if (usdtTicker != null) {
      results.add("$" + decimalFormat.format(usdtTicker.getLastPrice()));
    }
    if (btcTicker != null) {
      results.add("\u20BF" + btcTicker.getLastPrice().toPlainString());
    }
    if (ethTicker != null) {
      results.add("Ξ" + ethTicker.getLastPrice().toPlainString());
    }
    return results.stream().collect(Collectors.joining("\n"));
  }

  private String getColor(CoinMarketCapTicker ticker) {
    BigDecimal percentChange24Hour = ticker.getPctChange24h();
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

  private CoinMarketCapTicker getCoinMarketCapTicker(Exchange exchange, CurrencyPair currencyPair) {
    CoinMarketCapMarketDataService dataService = ((CoinMarketCapMarketDataService)(exchange.getMarketDataService()));
    try {
      for (CoinMarketCapTicker t : dataService.getCoinMarketCapTickers()) {
        if (t.getBaseCurrency().getCurrency().equals(currencyPair.base)) {
          return t;
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return null;
  }
}
