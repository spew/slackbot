package org.poker.listener;

import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.junit.Test;
import org.poker.listener.strategy.ChannelListeningStrategy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ChannelAwareMessageListenerTest {
    @Test
    public void shouldIgnore() {
        SlackMessagePostedListener listener = mock(SlackMessagePostedListener.class);
        ChannelListeningStrategy strategy = mock(ChannelListeningStrategy.class);
        when(strategy.shouldHandleEvent(any(), any())).thenReturn(false);
        ChannelAwareMessageListener channelAwareMessageListener = new ChannelAwareMessageListener(listener, strategy);
        channelAwareMessageListener.onEvent(null, null);
        verify(listener, never()).onEvent(any(), any());
    }

    @Test
    public void shouldHandle() {
        SlackMessagePostedListener listener = mock(SlackMessagePostedListener.class);
        ChannelListeningStrategy strategy = mock(ChannelListeningStrategy.class);
        when(strategy.shouldHandleEvent(any(), any())).thenReturn(true);
        ChannelAwareMessageListener channelAwareMessageListener = new ChannelAwareMessageListener(listener, strategy);
        channelAwareMessageListener.onEvent(null, null);
        verify(listener, times(1)).onEvent(any(), any());
    }
}
