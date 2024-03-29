package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.poker.config.ApplicationConfiguration;
import org.poker.config.Stage;
import org.poker.coronavirus.VirusStatsRetriever;
import org.poker.listener.strategy.ChannelListeningStrategy;
import org.poker.listener.strategy.ChannelListeningStrategyFactory;
import org.poker.stock.CachingLogoURLRetriever;
import org.poker.stock.GoogleImagesLogoURLRetriever;
import org.poker.stock.YahooFinanceStockResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultListenerAdder {
    private final ApplicationConfiguration applicationConfiguration;

    public DefaultListenerAdder(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    public void addMessagePostedListeners(SlackSession slackSession) {
        for (SlackMessagePostedListener l : createListeners()) {
            slackSession.addMessagePostedListener(l);
        }
    }

    private List<SlackMessagePostedListener> createListeners() {
        List<SlackMessagePostedListener> listeners = new ArrayList<>();
        listeners.add(new CryptoCurrencyMessageListener(
                applicationConfiguration.getCoinMarketCapApiKey(),
                applicationConfiguration.getBinanceApiKey(),
                applicationConfiguration.getBinanceSecretKey()));
        listeners.add(new StockQuoteMessageListener(new YahooFinanceStockResolver(), new CachingLogoURLRetriever(new GoogleImagesLogoURLRetriever())));
        listeners.add(new VirusMessageListener(new VirusStatsRetriever()));
        listeners.add(new ChessListener());
        Stage stage = applicationConfiguration.getStage();
        ChannelListeningStrategy channelListeningStrategy = ChannelListeningStrategyFactory.newDefault(stage);
        return listeners.stream().map(l -> new ChannelAwareMessageListener(l, channelListeningStrategy)).collect(Collectors.toList());
    }
}
