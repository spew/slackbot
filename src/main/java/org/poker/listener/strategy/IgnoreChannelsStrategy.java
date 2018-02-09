package org.poker.listener.strategy;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class IgnoreChannelsStrategy implements ChannelListeningStrategy {
  private final Set<String> channels = new HashSet<>();

  public IgnoreChannelsStrategy(Collection<String> channels) {
    this.channels.addAll(channels);
  }

  @Override
  public boolean shouldHandleEvent(SlackMessagePosted event, SlackSession session) {
    return !channels.contains(event.getChannel().getName());
  }
}
