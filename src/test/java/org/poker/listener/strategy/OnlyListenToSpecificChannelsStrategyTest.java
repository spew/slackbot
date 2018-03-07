package org.poker.listener.strategy;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OnlyListenToSpecificChannelsStrategyTest {

    @Test
    public void acceptGeneral() {
        OnlyListenToSpecificChannelsStrategy strategy = new OnlyListenToSpecificChannelsStrategy(Arrays.asList("general"));
        assertTrue(strategy.shouldHandleEvent(mockEvent("general"), null));
    }

    @Test
    public void ignoreBot() {
        OnlyListenToSpecificChannelsStrategy strategy = new OnlyListenToSpecificChannelsStrategy(Arrays.asList("general"));
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
