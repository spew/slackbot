package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.poker.listener.strategy.ChannelListeningStrategy;

public class ChannelAwareMessageListener implements SlackMessagePostedListener {
    private final SlackMessagePostedListener listener;
    private final ChannelListeningStrategy channelListeningStrategy;

    public ChannelAwareMessageListener(SlackMessagePostedListener listener, ChannelListeningStrategy channelListeningStrategy) {
        this.listener = listener;
        this.channelListeningStrategy = channelListeningStrategy;
    }

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {
        if (channelListeningStrategy.shouldHandleEvent(event, session)) {
            try {
                listener.onEvent(event, session);
            } catch (Exception e) {
                session.sendMessage(event.getChannel(), formatExceptionMessage(e));
            }
        }
    }

    private String formatExceptionMessage(Exception e) {
        String listenerName = listener.getClass().getName();
        String stackTrace = ExceptionUtils.getStackTrace(e);
        int idx = StringUtils.ordinalIndexOf(stackTrace, "\n", 3);
        if (idx > 0) {
            stackTrace = stackTrace.substring(0, idx);
        }
        // indent the stacktrace
        stackTrace = stackTrace.replaceAll("(?m)^", "  ");
        return String.format("Unhandled exception in %s: %s\n%s", listenerName, e.getMessage(), stackTrace);
    }
}
