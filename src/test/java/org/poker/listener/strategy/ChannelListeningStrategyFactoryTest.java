package org.poker.listener.strategy;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Test;
import org.poker.TestMocks;
import org.poker.config.Stage;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChannelListeningStrategyFactoryTest {
    @Test
    public void productionStageAndClass() {
        assertStageAndClass(Stage.Production, SpecificChannelsAndDMsListeningStrategy.class);
    }

    @Test
    public void gammaStageAndClass() {
        assertStageAndClass(Stage.Gamma, IgnoreChannelsListeningStrategy.class);
    }

    @Test
    public void productionShouldUseGeneral() {
        assertStageAndChannelIsUsed(Stage.Production, "general");
    }

    @Test
    public void productionShouldUseDaCoins() {
        assertStageAndChannelIsUsed(Stage.Production, "dacoins");
    }

    @Test
    public void productionShouldIgnoreBot() {
        assertStageAndChannelIsNotUsed(Stage.Production, "bot");
    }

    @Test
    public void gammaShouldIgnoreGeneral() {
        assertStageAndChannelIsNotUsed(Stage.Gamma, "general");
    }

    @Test
    public void gammaShouldIgnoreDaCoins() {
        assertStageAndChannelIsNotUsed(Stage.Gamma, "dacoins");
    }

    @Test
    public void gammaShouldUseBot() {
        assertStageAndChannelIsUsed(Stage.Gamma, "bot");
    }

    private void assertStageAndChannelIsNotUsed(Stage stage, String channelName) {
        ChannelListeningStrategy strategy = ChannelListeningStrategyFactory.newDefault(stage);
        SlackMessagePosted event = TestMocks.mockMessagePosted(channelName);
        assertFalse(strategy.shouldHandleEvent(event, null));
    }

    private void assertStageAndChannelIsUsed(Stage stage, String channelName) {
        ChannelListeningStrategy strategy = ChannelListeningStrategyFactory.newDefault(stage);
        SlackMessagePosted event = TestMocks.mockMessagePosted(channelName);
        assertTrue(strategy.shouldHandleEvent(event, null));
    }

    private void assertStageAndClass(Stage stage, Class klass) {
        ChannelListeningStrategy strategy = ChannelListeningStrategyFactory.newDefault(stage);
        assertThat(strategy, instanceOf(klass));
    }
}
