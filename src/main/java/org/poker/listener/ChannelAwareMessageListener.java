package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.poker.listener.strategy.ChannelListeningStrategy;

public class ChannelAwareMessageListener implements SlackMessagePostedListener {
  private final SlackMessagePostedListener listener;
  private final ChannelListeningStrategy channelListeningStrategy;

  public ChannelAwareMessageListener(SlackMessagePostedListener listener, ChannelListeningStrategy channelListeningStrategy) {
    this.listener = listener;
    this.channelListeningStrategy = channelListeningStrategy;
  }

  @Override
  public void onEvent(SlackMessagePosted event, SlackSession session) {
    if (channelListeningStrategy.shouldHandleEvent(event, session)) {
      listener.onEvent(event, session);
    }
  }
}
