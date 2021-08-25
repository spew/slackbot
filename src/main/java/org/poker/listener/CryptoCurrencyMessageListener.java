package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import humanize.ICUHumanize;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.binance.service.BinanceMarketDataServiceRaw;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.coinbasepro.dto.marketdata.CoinbaseProProductTicker;
import org.knowm.xchange.coinbasepro.service.CoinbaseProMarketDataService;
import org.knowm.xchange.coinmarketcap.pro.v1.CmcExchange;
import org.knowm.xchange.coinmarketcap.pro.v1.dto.marketdata.CmcCurrency;
import org.knowm.xchange.coinmarketcap.pro.v1.dto.marketdata.CmcQuote;
import org.knowm.xchange.coinmarketcap.pro.v1.dto.marketdata.CmcTicker;
import org.knowm.xchange.coinmarketcap.pro.v1.service.CmcMarketDataService;
import org.knowm.xchange.currency.CurrencyPair;
import org.poker.cryptocurrency.CoinMarketCapIconURLRetriever;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

public class CryptoCurrencyMessageListener implements SlackMessagePostedListener {
    private static final DecimalFormat standardDecimalFormat = new DecimalFormat("#,###.00");
    private static final DecimalFormat lessThanZeroDecimalFormat = new DecimalFormat("0.#####");
    private static final Integer blackListThreshold = 3;

    private static final HashSet<String> coinBlacklist = new HashSet<String>() {};
    private static final HashMap<String, Integer> tickerBlacklistThresholdCounts = new HashMap<String, Integer>() {};

    private BinanceExchange binanceExchange;
    private CoinbaseProExchange gdaxExchange;
    private Exchange coinMarketCapExchange;

    private String coinMarketCapApiKey;
    private String binanceApiKey;
    private String binanceSecretKey;
    private List<CmcCurrency> cmcCurrencies = null;

    public CryptoCurrencyMessageListener(String coinMarketCapApiKey, String binanceApiKey, String binanceSecretKey) {
        this.coinMarketCapApiKey = coinMarketCapApiKey;
        this.binanceApiKey = binanceApiKey;
        this.binanceSecretKey = binanceSecretKey;
    }

    private synchronized void ensureExchanges() {
        // it is a little dangerous to make web requests inside a synchronized method -- should improve that
        if (coinMarketCapExchange == null) {
            ExchangeSpecification coinMarketCapExchangeSpecification = new CmcExchange().getDefaultExchangeSpecification();
            coinMarketCapExchangeSpecification.setApiKey(coinMarketCapApiKey);
            coinMarketCapExchange = ExchangeFactory.INSTANCE.createExchange(coinMarketCapExchangeSpecification);
        }
        if (binanceExchange == null) {
            ExchangeSpecification binanceExchangeSpecification = new BinanceExchange().getDefaultExchangeSpecification();
            binanceExchangeSpecification.setApiKey(binanceApiKey);
            binanceExchangeSpecification.setSecretKey(binanceSecretKey);
            binanceExchange = (BinanceExchange) ExchangeFactory.INSTANCE.createExchange(binanceExchangeSpecification);
        }
        if (gdaxExchange == null) {
            gdaxExchange = (CoinbaseProExchange) ExchangeFactory.INSTANCE.createExchange(new CoinbaseProExchange().getDefaultExchangeSpecification());
        }
    }

    public void onEvent(SlackMessagePosted event, SlackSession session) {
        String message = event.getMessageContent();
        List<String> tickers = getTickers(message);
        if (tickers.isEmpty()) {
            return;
        }
        ensureExchanges();
        List<SlackAttachment> attachments = new ArrayList<>();
        for (String coin : tickers) {
            if(isBlacklisted(coin)) continue;
            CmcCurrency cmcCurrency = getCmcCurrency(coin);
            SlackAttachment attachment = generateAttachment(coin, cmcCurrency);
            attachments.add(attachment);
        }
        if (attachments.isEmpty()) {
            return;
        }
        SlackPreparedMessage preparedMessage = new SlackPreparedMessage.Builder()
                .withAttachments(attachments)
                .build();
        session.sendMessage(event.getChannel(), preparedMessage);
    }

    private SlackAttachment generateAttachment(String coin, CmcCurrency cmcCurrency) {
        if (cmcCurrency == null) {
            updateBlacklistThresholdCount(coin);
            if(isBlacklisted(coin)) {
                return new SlackAttachment(coin, null, String.format("No currency found with symbol '%s. Blacklist threshold met, blacklisting.'", coin), null);
            }
            return new SlackAttachment(coin, null, String.format("No currency found with symbol '%s.", coin), null);
        }
        CurrencyPair currencyPair = new CurrencyPair(cmcCurrency.getSymbol(), "USD");
        CmcTicker cmcTicker = getCmcTicker(currencyPair);
        CmcQuote quote = cmcTicker.getQuote().get(currencyPair.counter.getCurrencyCode());
        return formatAttachment(cmcCurrency.getSymbol(), cmcTicker, quote);
    }

    private boolean isBlacklisted(String coin) {
        return coinBlacklist.contains(coin);
    }

    private void updateBlacklistThresholdCount(String coin) {
        if(tickerBlacklistThresholdCounts.containsKey(coin)) {
            Integer newCount = tickerBlacklistThresholdCounts.get(coin) + 1;
            tickerBlacklistThresholdCounts.put(coin, newCount);
            if (newCount.equals(blackListThreshold)) {
                coinBlacklist.add(coin);
            }
        } else {
            tickerBlacklistThresholdCounts.put(coin, 1);
        }
    }

    public List<String> getTickers(String message) {
        List<String> results = new ArrayList<>();
        String[] tokens = message.split(" ");
        for (String t : tokens) {
            String ticker = validateToken(t);
            if (ticker != null) {
                results.add(ticker);
            }
        }
        return results;
    }

    private String validateToken(String t) {
        t = t.trim();
        if (t.startsWith(".") && t.length() >= 2 && t.length() <= 10) {
            t = t.substring(1).trim();
            if (StringUtils.isAlphanumeric(t)) {
                return t;
            }
        }
        return null;
    }

    private CmcTicker getCmcTicker(CurrencyPair currencyPair) {
        CmcMarketDataService cmcMarketDataService = (CmcMarketDataService)coinMarketCapExchange.getMarketDataService();
        try {
            return cmcMarketDataService.getCmcLatestQuote(currencyPair).get(currencyPair.base.getCurrencyCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CmcCurrency getCmcCurrency(String coin) {
        List<CmcCurrency> cmcCurrencies = getCmcCurrencies();
        for (CmcCurrency currency : cmcCurrencies) {
            if (currency.getSymbol().equalsIgnoreCase(coin)) {
                return currency;
            }
        }
        return null;
    }

    private List<CmcCurrency> getCmcCurrencies() {
        synchronized (this) {
            if (this.cmcCurrencies != null) {
                return this.cmcCurrencies;
            }
        }
        CmcMarketDataService cmcMarketDataService = (CmcMarketDataService)coinMarketCapExchange.getMarketDataService();
        try {
            List<CmcCurrency> results = cmcMarketDataService.getCmcCurrencyList();
            synchronized (this) {
                if (this.cmcCurrencies == null) {
                    this.cmcCurrencies = results;
                }
                return this.cmcCurrencies;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SlackAttachment formatAttachment(String baseCurrencyCode, CmcTicker ticker, CmcQuote quote) {
        SlackAttachment attachment = new SlackAttachment();
        attachment.setFallback(formatTitle(ticker) + " " + formatMessage(quote));
        attachment.setColor(getColor(quote));
        Optional<String> thumbUrl = new CoinMarketCapIconURLRetriever().retrieve(ticker.getSymbol());
        thumbUrl.ifPresent(attachment::setThumbUrl);
        attachment.addField(formatTitle(ticker), formatMessage(quote), false);
        attachment.addField("Binance", formatBinancePrices(baseCurrencyCode), true);
        if (shouldIncludeCoinbase(baseCurrencyCode)) {
            attachment.addField("CoinbasePro", formatCoinbasePrices(baseCurrencyCode), true);
        }
        return attachment;
    }

    private String formatTitle(CmcTicker ticker) {
        return String.format("%s (%s)", ticker.getName(), ticker.getSymbol());
    }

    private String formatMessage(CmcQuote quote) {
        BigDecimal usdPrice = quote.getPrice();
        BigDecimal percentChange24Hour = quote.getPercentChange24h();
        boolean isZeroOrPositive = percentChange24Hour.compareTo(BigDecimal.ZERO) >= 0;
        return String.format("$%s (%s%s%%) | Cap: %s | Vol: %s",
                formatPrice(usdPrice),
                isZeroOrPositive ? "+" : "",
                formatPercentage(percentChange24Hour),
                "$" + ICUHumanize.compactDecimal(quote.getMarketCap()),
                ICUHumanize.compactDecimal(quote.getVolume24h()));
    }

    private String getCurrencyCodeFromMessage(String message) {
        if (message.equalsIgnoreCase(".ether")) {
            return "ETH";
        }
        return message.substring(1);
    }

    private BinanceTicker24h getBinanceTicker(String baseCurrencyCode, String counterCurrencyCode) {
        BinanceMarketDataServiceRaw dataService = (BinanceMarketDataService) binanceExchange.getMarketDataService();
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

    private boolean shouldIncludeCoinbase(String baseCurrencyCode) {
        switch (baseCurrencyCode.toLowerCase()) {
            case "eth":
            case "btc":
            case "ltc":
            case "bch":
                return true;
            default:
                return false;
        }
    }

    private CoinbaseProProductTicker getCoinbaseProTicker(String baseCurrencyCode, String counterCurrencyCode) {
        CoinbaseProMarketDataService marketDataService = (CoinbaseProMarketDataService) gdaxExchange.getMarketDataService();
        CurrencyPair cp = getCurrencyPairForExchange(gdaxExchange, baseCurrencyCode, counterCurrencyCode);
        if (cp == null) {
            return null;
        }
        try {
            return marketDataService.getCoinbaseProProductTicker(cp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatCoinbasePrices(String baseCurrencyCode) {
        CoinbaseProProductTicker usdtTicker = getCoinbaseProTicker(baseCurrencyCode, "USD");
        CoinbaseProProductTicker btcTicker = getCoinbaseProTicker(baseCurrencyCode, "BTC");
        CoinbaseProProductTicker ethTicker = getCoinbaseProTicker(baseCurrencyCode, "ETH");
        List<String> results = new ArrayList<>();
        if (usdtTicker != null) {
            results.add("$" + formatPrice(usdtTicker.getPrice()));
        }
        if (btcTicker != null) {
            results.add("\u0E3F" + btcTicker.getPrice().toPlainString());
        }
        if (ethTicker != null) {
            results.add("Ξ" + ethTicker.getPrice().toPlainString());
        }
        return results.stream().collect(Collectors.joining("\n"));
    }

    private String formatBinancePrices(String baseCurrencyCode) {
        BinanceTicker24h usdtTicker = getBinanceTicker(baseCurrencyCode, "USDT");
        BinanceTicker24h btcTicker = getBinanceTicker(baseCurrencyCode, "BTC");
        BinanceTicker24h ethTicker = getBinanceTicker(baseCurrencyCode, "ETH");
        List<String> results = new ArrayList<>();
        if (usdtTicker != null) {
            results.add("$" + formatPrice(usdtTicker.getLastPrice()));
        }
        if (btcTicker != null) {
            results.add("\u0E3F" + formatBinancePrice(btcTicker));
        }
        if (ethTicker != null) {
            results.add("Ξ" + formatBinancePrice(ethTicker));
        }
        return results.stream().collect(Collectors.joining("\n"));
    }

    private String formatBinancePrice(BinanceTicker24h ticker) {
        BigDecimal priceChangePercent = ticker.getPriceChangePercent();
        boolean isZeroOrPositive = priceChangePercent.compareTo(BigDecimal.ZERO) >= 0;

        return String.format("%s (%s%s%%)",
                ticker.getLastPrice().toPlainString(),
                isZeroOrPositive ? "+" : "",
                formatPercentage(priceChangePercent));
    }

    private String getColor(CmcQuote quote) {
        BigDecimal percentChange24Hour = quote.getPercentChange24h();
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

    private static String formatPrice(BigDecimal decimal) {
        if (decimal.compareTo(BigDecimal.ONE) < 0) {
            return lessThanZeroDecimalFormat.format(decimal);
        }
        return standardDecimalFormat.format(decimal);
    }

    private static String formatPercentage(BigDecimal percentage) {
        return standardDecimalFormat.format(percentage);
    }
}
