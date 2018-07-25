package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.binance.service.BinanceMarketDataServiceRaw;
import org.knowm.xchange.coinmarketcap.CoinMarketCapExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.poker.cryptocurrency.CoinMarketCapIconURLRetriever;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VechainTempListener implements SlackMessagePostedListener {
    private List<Exchange> exchanges = new ArrayList<>();
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,##0.000");
    private static final DecimalFormat percentFormat = new DecimalFormat("#,##0.00");

    private BinanceExchange binanceExchange;

    public VechainTempListener() {
        ExchangeSpecification coinMarketCapExchangeSpecification = new CoinMarketCapExchange().getDefaultExchangeSpecification();
        exchanges.add(ExchangeFactory.INSTANCE.createExchange(coinMarketCapExchangeSpecification));
        ExchangeSpecification binanceExchangeSpecification = new BinanceExchange().getDefaultExchangeSpecification();
        binanceExchange = (BinanceExchange) ExchangeFactory.INSTANCE.createExchange(binanceExchangeSpecification);
    }

    public void onEvent(SlackMessagePosted event, SlackSession session) {
        String message = event.getMessageContent().trim();
        if (!message.equalsIgnoreCase(".vet") && !message.equalsIgnoreCase(".vtho")) {
            return;
        }
        String coin = getCurrencyCodeFromMessage(message);
        SlackChannel channel = event.getChannel();
        for (Exchange e : exchanges) {
            List<CurrencyPair> currencyPairs = e.getExchangeSymbols();
            for (CurrencyPair cp : currencyPairs) {
                if (cp.base.getCurrencyCode().equalsIgnoreCase(coin) && cp.counter.getCurrencyCode().equals("USD")) {
                    // Coinmarketcap added support. This listener can be removed.
                    return;
                }
            }
        }

        BinanceTicker24h ticker = getBinanceTicker(coin, "USDT");
        if (ticker != null) {
            SlackAttachment attachment = formatAttachment(ticker);
            session.sendMessage(channel, attachment.getFallback(), attachment);
        }
    }

    private SlackAttachment formatAttachment(BinanceTicker24h ticker) {
        SlackAttachment attachment = new SlackAttachment();
        attachment.setFallback(formatMessage(ticker));
        attachment.setColor(getColor(ticker.getPriceChangePercent()));
        Optional<String> thumbUrl = new CoinMarketCapIconURLRetriever().retrieve("vechain");
        thumbUrl.ifPresent(attachment::setThumbUrl);
        attachment.addField("Binance", formatBinancePrices("vet"), true);

        return attachment;
    }

    private String formatMessage(BinanceTicker24h ticker) {
        String currencyCode = ticker.getCurrencyPair().base.getCurrencyCode();
        String strMessage = String.format("%s (%s)",
                getName(currencyCode),
                currencyCode);
        return strMessage;
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

    private String formatBinancePrices(String baseCurrencyCode) {
        BinanceTicker24h usdtTicker = getBinanceTicker(baseCurrencyCode, "USDT");
        BinanceTicker24h btcTicker = getBinanceTicker(baseCurrencyCode, "BTC");
        BinanceTicker24h ethTicker = getBinanceTicker(baseCurrencyCode, "ETH");
        List<String> results = new ArrayList<>();
        if (usdtTicker != null) {
            results.add("$" + formatBinancePrice(usdtTicker));
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
                percentFormat.format(priceChangePercent));
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

    private String getName(String coin) {
        return coin.equalsIgnoreCase("vet") ? "VeChainThor" : "VeThor";
    }
}
