package org.poker.poller.strategy;

import com.ullink.slack.simpleslackapi.SlackChannel;

public interface ChannelMessageStrategy {
    boolean shouldSend(SlackChannel channel);
}
