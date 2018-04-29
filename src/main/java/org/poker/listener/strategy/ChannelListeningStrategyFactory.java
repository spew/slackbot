package org.poker.listener.strategy;

import org.poker.config.ApplicationConfiguration;
import org.poker.config.Stage;

import java.util.Arrays;
import java.util.List;

public class ChannelListeningStrategyFactory {
    private static final String MAIN_CHANNEL = "general";
    private static final String COIN_CHANNEL = "dacoins";
    private static final List<String> PRODUCTION_CHANNELS = Arrays.asList(MAIN_CHANNEL, COIN_CHANNEL);

    private ChannelListeningStrategyFactory() {

    }

    public static ChannelListeningStrategy newDefault(Stage stage) {
        switch (stage) {
            case Production:
                return new SpecificChannelsAndDMsListeningStrategy(PRODUCTION_CHANNELS);
            case Gamma:
            default:
                return new IgnoreChannelsListeningStrategy(PRODUCTION_CHANNELS);
        }
    }

    public static ChannelListeningStrategy newDefault(ApplicationConfiguration applicationConfiguration) {
        return newDefault(applicationConfiguration.getStage());
    }
}
