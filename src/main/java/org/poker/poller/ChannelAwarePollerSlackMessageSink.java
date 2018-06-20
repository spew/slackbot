package org.poker.poller;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import org.poker.poller.strategy.ChannelMessageStrategy;

public class ChannelAwarePollerSlackMessageSink implements PollerSlackMessageSink {
    private final SlackSession slackSession;
    private final ChannelMessageStrategy strategy;

    public ChannelAwarePollerSlackMessageSink(SlackSession slackSession, ChannelMessageStrategy strategy) {
        this.slackSession = slackSession;
        this.strategy = strategy;
    }

    @Override
    public void SendMessage(SlackAttachment slackAttachment) {
        for (SlackChannel slackChannel : slackSession.getChannels()) {
            if (strategy.shouldSend(slackChannel)) {
                slackSession.sendMessage(slackChannel, slackAttachment.getFallback(), slackAttachment);
            }
        }
    }
}
