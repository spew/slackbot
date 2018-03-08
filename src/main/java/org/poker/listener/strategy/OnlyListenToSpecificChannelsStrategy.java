package org.poker.listener.strategy;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OnlyListenToSpecificChannelsStrategy implements ChannelListeningStrategy {
    private final Set<String> channelNames = new HashSet<>();

    public OnlyListenToSpecificChannelsStrategy(Collection<String> channelNames) {
        this.channelNames.addAll(channelNames);
    }

    @Override
    public boolean shouldHandleEvent(SlackMessagePosted event, SlackSession session) {
        return event.getChannel().isDirect() || channelNames.contains(event.getChannel().getName());
    }
}
