package org.poker.listener.strategy;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

public interface ChannelListeningStrategy {
    boolean shouldHandleEvent(SlackMessagePosted event, SlackSession session);
}
