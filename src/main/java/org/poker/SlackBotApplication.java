package org.poker;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.poker.listener.ChannelAwareMessageListener;
import org.poker.listener.CryptoCurrencyMessageListener;
import org.poker.listener.StockQuoteMessageListener;
import org.poker.listener.strategy.ChannelListeningStrategy;
import org.poker.listener.strategy.ChannelListeningStrategyFactory;
import org.poker.slack.SlackApiTokenValidator;
import org.poker.stock.YahooFinanceStockResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SlackBotApplication {
    private static final Logger logger = LogManager.getLogger(SlackBotApplication.class);
    private ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    public void run() throws IOException, InterruptedException {
        SlackSession session = connect();
        addMessagePostedListeners(session);
        runLoop();
    }

    private SlackSession connect() throws IOException {
        String apiToken = applicationConfiguration.getSlackApiToken();
        new SlackApiTokenValidator().validate(apiToken);
        SlackSession session = SlackSessionFactory.createWebSocketSlackSession(apiToken);
        session.connect();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Executing slack shutdown hook.");
            try {
                session.disconnect();
                logger.info("Disconnected from slack.");
            } catch (Exception e) {
                logger.error("Error disconnecting from slack session", e);
            }
        }));
        return session;
    }

    private void runLoop() throws InterruptedException {
        while (true) {
            Thread.sleep(1000);
        }
    }

    private void addMessagePostedListeners(SlackSession slackSession) {
        for (SlackMessagePostedListener l : createListeners()) {
            slackSession.addMessagePostedListener(l);
        }
    }

    private List<SlackMessagePostedListener> createListeners() {
        List<SlackMessagePostedListener> listeners = new ArrayList<>();
        listeners.add(new CryptoCurrencyMessageListener());
        listeners.add(new StockQuoteMessageListener(new YahooFinanceStockResolver()));
        Stage stage = applicationConfiguration.getStage();
        ChannelListeningStrategy channelListeningStrategy = ChannelListeningStrategyFactory.newDefault(stage);
        return listeners.stream().map(l -> new ChannelAwareMessageListener(l, channelListeningStrategy)).collect(Collectors.toList());
    }
}
