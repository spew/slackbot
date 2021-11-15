package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.poker.coronavirus.VirusStats;
import org.poker.coronavirus.VirusStatsRetriever;
import org.poker.youtube.YoutubeLiveBroadcastSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VirusMessageListener implements SlackMessagePostedListener {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeLiveBroadcastSearcher.class);

    private static final DecimalFormat oneDigitPrecisionDecimalFormat = new DecimalFormat("#,##0.0");
    private final VirusStatsRetriever statsRetriever;

    public VirusMessageListener(VirusStatsRetriever statsRetriever) {
        this.statsRetriever = statsRetriever;
    }

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession session) {
        if (!shouldHandle(event.getMessageContent())) {
            return;
        }
        VirusStats worldStats = statsRetriever.retrieve();
        VirusStats usStats = statsRetriever.retrieveUSA();
        Date newTime = new Date();
        SlackPreparedMessage message = formatMessage(newTime, worldStats, usStats);
        session.sendMessage(event.getChannel(), message);
    }

    private boolean shouldHandle(String message) {
        switch (message) {
            case ".wuhan":
            case ".corona":
            case ".virus":
            case ".coronavirus":
                return true;
            default:
                return false;
        }
    }

    private SlackPreparedMessage formatMessage(Date curDate, VirusStats curWorldStats, VirusStats curUSStats) {
        SlackAttachment attachment = new SlackAttachment();
        addField(attachment, "Worldwide Cases", curWorldStats.getTotalCases(), curWorldStats.getTotalCasesDelta(),
                calcPercentageDifference(curWorldStats.getTotalCases(), curWorldStats.getTotalCases() - curWorldStats.getTotalCasesDelta()));
        addField(attachment, "Worldwide Deaths", curWorldStats.getDeaths(), curWorldStats.getDeathsDelta(),
                calcPercentageDifference(curWorldStats.getDeaths(), curWorldStats.getDeaths() - curWorldStats.getDeathsDelta()));
        List<SlackAttachment> attachments = new ArrayList<>();
        attachments.add(attachment);
        attachment = new SlackAttachment();
        addField(attachment, "US Cases", curUSStats.getTotalCases(), curUSStats.getTotalCasesDelta(),
                calcPercentageDifference(curUSStats.getTotalCases(), curUSStats.getTotalCases() - curUSStats.getTotalCasesDelta()));
        addField(attachment, "US Deaths", curUSStats.getDeaths(), curUSStats.getDeathsDelta(),
                calcPercentageDifference(curUSStats.getDeaths(), curUSStats.getDeaths() - curUSStats.getDeathsDelta()));
        attachments.add(attachment);
        SlackPreparedMessage preparedMessage = SlackPreparedMessage.builder()
                .message("Relative to yesterday:")
                .attachments(attachments)
                .build();
        return preparedMessage;
    }

    private void addField(SlackAttachment attachment, String title, int curValue, int diff, double percentageDiff) {
        attachment.addField(title,
                String.format("%s +%s (+%s%%)", formatInteger(curValue), formatInteger(diff), formatPercentage(percentageDiff)),
                true
        );
    }

    private double calcPercentageDifference(int currentValue, int prevValue) {
        int diff = currentValue - prevValue;
        return Integer.valueOf(diff).doubleValue() / prevValue * 100;
    }

    private String formatPercentage(double percentage) {
        return oneDigitPrecisionDecimalFormat.format(percentage);
    }

    private String formatInteger(int value) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        return numberFormat.format(value);
    }
}
