package org.poker.poller.strategy;

import org.poker.config.ApplicationConfiguration;
import org.poker.config.Channel;
import org.poker.config.Stage;

import java.util.Collections;
import java.util.List;

public class ChannelMessageStrategyFactory {

    private ChannelMessageStrategyFactory() {

    }

    public static ChannelMessageStrategy newDefault(Stage stage) {
        switch (stage) {
            case Production:
                return new SpecificChannelsAndDMMessageStrategy(Collections.singletonList(Channel.MAIN_CHANNEL));
            case Gamma:
            default:
                return new SpecificChannelsAndDMMessageStrategy(Channel.GAMMA_CHANNELS);
        }
    }

    public static ChannelMessageStrategy newDefault(ApplicationConfiguration applicationConfiguration) {
        return newDefault(applicationConfiguration.getStage());
    }

    public static ChannelMessageStrategy newSpecificChannelsStrategy(Stage stage, List<String> channels) {
        switch (stage) {
            case Production:
                return new SpecificChannelsAndDMMessageStrategy(channels);
            case Gamma:
            default:
                return new SpecificChannelsAndDMMessageStrategy(Channel.GAMMA_CHANNELS);
        }
    }
}
