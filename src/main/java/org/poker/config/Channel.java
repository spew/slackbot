package org.poker.config;

import java.util.Arrays;
import java.util.List;

public class Channel {
    public static final String MAIN_CHANNEL = "general";
    public static final String COIN_CHANNEL = "dacoins";
    public static final String STOCK_CHANNEL = "dastocks";
    public static final List<String> PRODUCTION_CHANNELS = Arrays.asList(MAIN_CHANNEL, COIN_CHANNEL, STOCK_CHANNEL);

    public static final String BOT_CHANNEL = "bot";
    public static final List<String> GAMMA_CHANNELS = Arrays.asList(BOT_CHANNEL);

    private Channel() {

    }
}
