package org.poker;

import org.junit.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.coinmarketcap.pro.v1.CmcExchange;
import org.knowm.xchange.coinmarketcap.pro.v1.dto.marketdata.CmcCurrency;
import org.knowm.xchange.coinmarketcap.pro.v1.dto.marketdata.CmcQuote;
import org.knowm.xchange.coinmarketcap.pro.v1.dto.marketdata.CmcTicker;
import org.knowm.xchange.coinmarketcap.pro.v1.service.CmcMarketDataService;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public class CmcExchangeTest {

    @Test
    public void TestCmcExchange() {
        ExchangeSpecification coinMarketCapExchangeSpecification = new CmcExchange().getDefaultExchangeSpecification();
        coinMarketCapExchangeSpecification.setApiKey("626203aa-2c28-4498-84f6-d6e972a6f940");
        Exchange e = ExchangeFactory.INSTANCE.createExchange(coinMarketCapExchangeSpecification);
        CmcExchange cmcExchange = (CmcExchange)e;
        CmcMarketDataService cmcMarketDataService = (CmcMarketDataService)e.getMarketDataService();
        CurrencyPair currencyPair = new CurrencyPair("BTC", "USD");
        CmcTicker cmcTicker;
        Ticker ticker;
        List<CmcCurrency> cmcCurrencies;
        try {
            cmcTicker = cmcMarketDataService.getCmcLatestQuote(currencyPair).get(currencyPair.base.getCurrencyCode());
            ticker = cmcMarketDataService.getTicker(currencyPair);
            cmcCurrencies = cmcMarketDataService.getCmcCurrencyList();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        BigDecimal last = ticker.getLast();
        CmcQuote quote = cmcTicker.getQuote().get(currencyPair.counter.getCurrencyCode());
        System.out.println(cmcCurrencies);
        //CmcTicker cmcTicker = (CmcTicker)ticker;

        //Params params = new
        //tickers tickers = e.getMarketDataService().getTicker()
        List<CurrencyPair> currencyPairs = e.getExchangeSymbols();
        for (CurrencyPair cp : currencyPairs) {
        }
    }
}
