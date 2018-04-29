package org.poker;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestMocks {
    public static SlackMessagePosted mockMessagePosted(String channelName) {
        SlackChannel channel = mock(SlackChannel.class);
        when(channel.getName()).thenReturn(channelName);
        SlackMessagePosted messagePosted = mock(SlackMessagePosted.class);
        when(messagePosted.getChannel()).thenReturn(channel);
        return messagePosted;
    }
}
