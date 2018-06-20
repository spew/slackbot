package org.poker.listener.strategy;

import org.poker.config.ApplicationConfiguration;
import org.poker.config.Channel;
import org.poker.config.Stage;

import java.util.Arrays;
import java.util.List;

public class ChannelListeningStrategyFactory {

    private ChannelListeningStrategyFactory() {

    }

    public static ChannelListeningStrategy newDefault(Stage stage) {
        switch (stage) {
            case Production:
                return new SpecificChannelsAndDMsListeningStrategy(Channel.PRODUCTION_CHANNELS);
            case Gamma:
            default:
                return new IgnoreChannelsListeningStrategy(Channel.PRODUCTION_CHANNELS);
        }
    }

    public static ChannelListeningStrategy newDefault(ApplicationConfiguration applicationConfiguration) {
        return newDefault(applicationConfiguration.getStage());
    }
}
