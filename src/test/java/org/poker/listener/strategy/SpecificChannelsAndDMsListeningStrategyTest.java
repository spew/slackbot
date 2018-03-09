package org.poker.listener.strategy;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpecificChannelsAndDMsListeningStrategyTest {

    @Test
    public void acceptGeneral() {
        SpecificChannelsAndDMsListeningStrategy strategy = new SpecificChannelsAndDMsListeningStrategy(Arrays.asList("general"));
        assertTrue(strategy.shouldHandleEvent(mockEvent("general", false), null));
    }

    @Test
    public void ignoreBot() {
        SpecificChannelsAndDMsListeningStrategy strategy = new SpecificChannelsAndDMsListeningStrategy(Arrays.asList("general"));
        assertFalse(strategy.shouldHandleEvent(mockEvent("bot", false), null));
    }

    @Test
    public void acceptDM() {
        SpecificChannelsAndDMsListeningStrategy strategy = new SpecificChannelsAndDMsListeningStrategy(Arrays.asList());
        assertTrue(strategy.shouldHandleEvent(mockEvent("ABCDEFG", true), null));
    }

    private SlackMessagePosted mockEvent(String channelName, boolean isDirectMessage) {
        SlackChannel channel = mockChannel(channelName, isDirectMessage);
        SlackMessagePosted messagePosted = mock(SlackMessagePosted.class);
        when(messagePosted.getChannel()).thenReturn(channel);
        return messagePosted;
    }

    private SlackChannel mockChannel(String channelName, boolean isDirectMessage) {
        SlackChannel channel = mock(SlackChannel.class);
        when(channel.getName()).thenReturn(channelName);
        when(channel.isDirect()).thenReturn(isDirectMessage);
        return channel;
    }
}
