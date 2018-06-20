package org.poker.poller.strategy;

import org.poker.config.ApplicationConfiguration;
import org.poker.config.Channel;
import org.poker.config.Stage;

public class ChannelMessageStrategyFactory {

    private ChannelMessageStrategyFactory() {

    }

    public static ChannelMessageStrategy newDefault(Stage stage) {
        switch (stage) {
            case Production:
                return new SpecificChannelsAndDMMessageStrategy(Channel.PRODUCTION_CHANNELS);
            case Gamma:
            default:
                return new SpecificChannelsAndDMMessageStrategy(Channel.GAMMA_CHANNELS);
        }
    }

    public static ChannelMessageStrategy newDefault(ApplicationConfiguration applicationConfiguration) {
        return newDefault(applicationConfiguration.getStage());
    }
}
