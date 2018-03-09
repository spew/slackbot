package org.poker.listener.strategy;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IgnoreChannelsListeningStrategyTest {

    @Test
    public void ignoreGeneral() {
        IgnoreChannelsListeningStrategy strategy = new IgnoreChannelsListeningStrategy(Arrays.asList("general"));
        assertFalse(strategy.shouldHandleEvent(mockEvent("general"), null));
    }

    @Test
    public void acceptBot() {
        IgnoreChannelsListeningStrategy strategy = new IgnoreChannelsListeningStrategy(Arrays.asList("bot"));
        assertFalse(strategy.shouldHandleEvent(mockEvent("bot"), null));
    }

    private SlackMessagePosted mockEvent(String channelName) {
        SlackChannel channel = mock(SlackChannel.class);
        when(channel.getName()).thenReturn(channelName);
        SlackMessagePosted messagePosted = mock(SlackMessagePosted.class);
        when(messagePosted.getChannel()).thenReturn(channel);
        return messagePosted;
    }
}
