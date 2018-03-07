package org.poker.listener.strategy;

import org.junit.Test;
import org.poker.config.Stage;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

public class ChannelListeningStrategyFactoryTest {
    @Test
    public void production() {
        assertStageAndClass(Stage.Production, OnlyListenToSpecificChannelsStrategy.class);
    }

    @Test
    public void gamma() {
        assertStageAndClass(Stage.Gamma, IgnoreChannelsStrategy.class);
    }

    private void assertStageAndClass(Stage stage, Class klass) {
        ChannelListeningStrategy strategy = ChannelListeningStrategyFactory.newDefault(stage);
        assertThat(strategy, instanceOf(klass));
    }
}
