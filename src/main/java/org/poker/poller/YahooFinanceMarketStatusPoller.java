package org.poker.poller;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class YahooFinanceMarketStatusPoller implements Poller {
    private final PollerSlackMessageSink messageSink;
    private static final Logger logger = LogManager.getLogger(YahooFinanceMarketStatusPoller.class);

    private MarketStatusRequest.MarketStatus previousStatus;

    public YahooFinanceMarketStatusPoller(PollerSlackMessageSink messageSink) {
        this.messageSink = messageSink;
        this.previousStatus = null;
    }

    @Override
    public TimeUnit getIntervalTimeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    public long getIntervalValue() {
        return TimeUnit.MINUTES.toSeconds(0) + 30;
    }

    @Override
    public void Poll() {
        MarketStatusRequest.MarketStatus currentStatus = getCurrentMarketStatus();
        if (previousStatus != null && currentStatus != previousStatus) {
            messageSink.SendMessage(formatMessage(currentStatus));
        }
        previousStatus = currentStatus;
    }

    private MarketStatusRequest.MarketStatus getCurrentMarketStatus() {
        try {
            MarketStatusRequest request = new MarketStatusRequest();
            return request.getResult().get(0);
        } catch (IOException e) {
            logger.error("Error retrieving market status", e);
        }
        return previousStatus;
    }

    private SlackAttachment formatMessage(MarketStatusRequest.MarketStatus status) {
        SlackAttachment attachment = new SlackAttachment();
        if (status.equals(MarketStatusRequest.MarketStatus.OPEN)) {
            attachment.setTitle(":bell: :bell: :bell: Market is " + status.toString() +"! :bell: :bell: :bell:");
            attachment.setColor("good");
        } else {
            attachment.setColor("danger");
            attachment.setTitle(":no_bell: :no_bell: :no_bell: Market is " + status.toString() +"! :no_bell: :no_bell: :no_bell:");
        }
        return attachment;
    }


}
