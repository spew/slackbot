package org.poker;


import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.poker.listener.CryptoCurrencyMessageListener;
import org.poker.listener.StockQuoteMessageListener;

import java.io.IOException;

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
    session.addMessagePostedListener(new CryptoCurrencyMessageListener());
    session.addMessagePostedListener(new StockQuoteMessageListener());
    while (apiToken.equals(apiToken)) {
      Thread.sleep(1000);
    }
    session.disconnect();
  }
}
