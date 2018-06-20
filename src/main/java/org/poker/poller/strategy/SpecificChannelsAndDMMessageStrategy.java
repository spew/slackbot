package org.poker.poller.strategy;

import com.ullink.slack.simpleslackapi.SlackChannel;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class SpecificChannelsAndDMMessageStrategy implements ChannelMessageStrategy {
    private Set<String> channelNames;

    public SpecificChannelsAndDMMessageStrategy(Collection<String> channelNames) {
        this.channelNames = new HashSet<>(channelNames.stream().map(c -> c.toLowerCase()).collect(Collectors.toList()));
    }

    @Override
    public boolean shouldSend(SlackChannel channel) {
        if (channel.getName() == null) {
            return false;
        }
        return channel.isDirect() || channelNames.contains(channel.getName().toLowerCase());
    }
}
