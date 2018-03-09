package org.poker.listener.strategy;

import org.poker.config.ApplicationConfiguration;
import org.poker.config.Stage;

import java.util.Arrays;

public class ChannelListeningStrategyFactory {
    private static final String MAIN_CHANNEL = "general";

    private ChannelListeningStrategyFactory() {

    }

    public static ChannelListeningStrategy newDefault(Stage stage) {
        switch (stage) {
            case Production:
                return new SpecificChannelsAndDMsListeningStrategy(Arrays.asList(MAIN_CHANNEL));
            case Gamma:
            default:
                return new IgnoreChannelsListeningStrategy(Arrays.asList(MAIN_CHANNEL));
        }
    }

    public static ChannelListeningStrategy newDefault(ApplicationConfiguration applicationConfiguration) {
        return newDefault(applicationConfiguration.getStage());
    }
}
