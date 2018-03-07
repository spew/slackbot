package org.poker.listener.strategy;

import org.poker.ApplicationConfiguration;
import org.poker.Stage;

import java.util.Arrays;

public class ChannelListeningStrategyFactory {
    private static final String MAIN_CHANNEL = "general";

    private ChannelListeningStrategyFactory() {

    }

    public static ChannelListeningStrategy newDefault(Stage stage) {
        switch (stage) {
            case Production:
                return new OnlyListenToSpecificChannelsStrategy(Arrays.asList(MAIN_CHANNEL));
            case Gamma:
            default:
                return new IgnoreChannelsStrategy(Arrays.asList(MAIN_CHANNEL));
        }
    }

    public static ChannelListeningStrategy newDefault(ApplicationConfiguration applicationConfiguration) {
        return newDefault(applicationConfiguration.getStage());
    }
}
