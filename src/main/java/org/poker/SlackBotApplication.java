package org.poker;


import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.poker.listener.ChannelAwareMessageListener;
import org.poker.listener.CryptoCurrencyMessageListener;
import org.poker.listener.StockQuoteMessageListener;
import org.poker.listener.strategy.IgnoreChannelsStrategy;
import org.poker.stock.YahooFinanceStockResolver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SlackBotApplication {
  private static final Logger logger = LogManager.getLogger(SlackBotApplication.class);
  private ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

  public void run() throws IOException, InterruptedException {
    String apiToken = applicationConfiguration.getSlackApiToken();
    SlackSession session = SlackSessionFactory.createWebSocketSlackSession(apiToken);
    session.connect();
    String channelName = "bot";
    SlackChannel channel = session.findChannelByName("general");
    if (!channel.isMember()) {
      logger.error("Not a member of channel", channelName);
    }
    addMessagePostedListeners(session);
    while (apiToken.equals(apiToken)) {
      Thread.sleep(1000);
    }
    session.disconnect();
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
    switch(stage) {
      case Production:
        return listeners;
      case Gamma:
      default:
        IgnoreChannelsStrategy strategy = new IgnoreChannelsStrategy(Arrays.asList("general"));
        return listeners.stream().map(l -> new ChannelAwareMessageListener(l, strategy)).collect(Collectors.toList());
    }
  }
}
