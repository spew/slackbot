package org.poker.listener.strategy;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Test;
import org.poker.TestMocks;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IgnoreChannelsListeningStrategyTest {

    @Test
    public void ignoreGeneral() {
        IgnoreChannelsListeningStrategy strategy = new IgnoreChannelsListeningStrategy(Arrays.asList("general"));
        assertFalse(strategy.shouldHandleEvent(TestMocks.mockMessagePosted("general"), null));
    }

    @Test
    public void acceptBot() {
        IgnoreChannelsListeningStrategy strategy = new IgnoreChannelsListeningStrategy(Arrays.asList("bot"));
        assertFalse(strategy.shouldHandleEvent(TestMocks.mockMessagePosted("bot"), null));
    }
}
