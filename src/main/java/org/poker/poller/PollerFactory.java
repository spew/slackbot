package org.poker.poller;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.poker.config.ApplicationConfiguration;
import org.poker.poller.strategy.ChannelMessageStrategyFactory;
import org.poker.poller.strategy.SpecificChannelsAndDMMessageStrategy;
import org.poker.youtube.Channel;
import org.poker.youtube.YoutubeLiveBroadcastSearcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PollerFactory {
    private PollerFactory() {

    }

    public static List<Poller> newDefaultPollers(ApplicationConfiguration applicationConfiguration, SlackSession session) {
        List<Poller> list = new ArrayList<>();
        list.add(newYoutubePoller(Channel.LATB_CHANNEL_ID, applicationConfiguration, session));
        list.add(newYoutubePoller(Channel.BROGAN_CHANNEL_ID, applicationConfiguration, session));
        list.add(newYoutubePoller(Channel.DEADCO_CHANNEL_ID, applicationConfiguration, session));
        list.add(newMarketStatusPoller(session));
        return list;
    }

    private static YahooFinanceMarketStatusPoller newMarketStatusPoller(SlackSession session) {
        SpecificChannelsAndDMMessageStrategy stockChannelStrategy = new SpecificChannelsAndDMMessageStrategy(Collections.singleton(org.poker.config.Channel.STOCK_CHANNEL));
        ChannelAwarePollerSlackMessageSink messageSink = new ChannelAwarePollerSlackMessageSink(session, stockChannelStrategy);
        return new YahooFinanceMarketStatusPoller(messageSink);
    }

    public static PollerManager newDefaultPollerManager(ApplicationConfiguration applicationConfiguration, SlackSession session) {
        return new PollerManager(newDefaultPollers(applicationConfiguration, session));
    }

    private static YoutubeChannelPoller newYoutubePoller(String youtubeChannelId, ApplicationConfiguration applicationConfiguration, SlackSession slackSession) {
        YouTube youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), null)
                .setApplicationName("slackbot")
                .build();
        YoutubeLiveBroadcastSearcher liveBroadcastSearcher = new YoutubeLiveBroadcastSearcher(youtube, applicationConfiguration.getYoutubeApiKey());
        PollerSlackMessageSink messageSink = new ChannelAwarePollerSlackMessageSink(slackSession, ChannelMessageStrategyFactory.newDefault(applicationConfiguration));
        return new YoutubeChannelPoller(youtubeChannelId, liveBroadcastSearcher, messageSink);
    }
}
