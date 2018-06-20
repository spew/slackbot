package org.poker.poller;

import com.ullink.slack.simpleslackapi.SlackAttachment;
public interface PollerSlackMessageSink {
    void SendMessage(SlackAttachment slackAttachment);
}
