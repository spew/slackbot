package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import humanize.ICUHumanize;
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
    private Date anchorTime;
    private VirusStats anchorWorldStats;
    private VirusStats anchorUSStats;

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
        if (anchorWorldStats == null) {
            anchorWorldStats = worldStats;
            anchorUSStats = usStats;
            anchorTime = new Date();
        }
        Date newTime = new Date();
        SlackPreparedMessage message = formatMessage(newTime, anchorTime, worldStats, usStats);
        session.sendMessage(event.getChannel(), message);
        anchorWorldStats = worldStats;
        anchorUSStats = usStats;
        anchorTime = newTime;
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

    private SlackPreparedMessage formatMessage(Date curDate, Date prevDate, VirusStats curWorldStats, VirusStats curUSStats) {
        StatsDifference worldDiff = getStatsDifference(anchorWorldStats, curWorldStats);
        SlackAttachment attachment = new SlackAttachment();
        addField(attachment, "Worldwide Cases", curWorldStats.getTotal(), worldDiff.TotalDifference, worldDiff.TotalPercentage);
        addField(attachment, "Worldwide Deaths", curWorldStats.getDeaths(), worldDiff.DeathsDifference, worldDiff.DeathsPercentage);
        List<SlackAttachment> attachments = new ArrayList<>();
        attachments.add(attachment);
        StatsDifference usDiff = getStatsDifference(anchorUSStats, curUSStats);
        attachment = new SlackAttachment();
        addField(attachment, "US Cases", curUSStats.getTotal(), usDiff.TotalDifference, usDiff.TotalPercentage);
        addField(attachment, "US Deaths", curUSStats.getDeaths(), usDiff.DeathsDifference, usDiff.DeathsPercentage);
        attachments.add(attachment);
        String naturalTime = ICUHumanize.naturalTime(prevDate, curDate, Locale.US);
        naturalTime = naturalTime.replace("from now", "ago");
        SlackPreparedMessage preparedMessage = new SlackPreparedMessage.Builder()
                .withMessage(String.format("Relative to %s:", naturalTime))
                .withAttachments(attachments)
                .build();
        return preparedMessage;
    }

    private void addField(SlackAttachment attachment, String title, int curValue, int diff, double percentageDiff) {
        attachment.addField(title,
                String.format("%s +%s (+%s%%)", formatInteger(curValue), formatInteger(diff), formatPercentage(percentageDiff)),
                true
        );
    }

    private StatsDifference getStatsDifference(VirusStats before, VirusStats now) {
        StatsDifference diff = new StatsDifference();
        diff.TotalDifference = now.getTotal() - before.getTotal();
        diff.DeathsDifference = now.getDeaths() - before.getDeaths();
        diff.RecoveriesDifference = now.getRecoveries() - before.getRecoveries();
        diff.TotalPercentage = calcPercentageDifference(now.getTotal(), before.getTotal());
        diff.DeathsPercentage = calcPercentageDifference(now.getDeaths(), before.getDeaths());
        diff.RecoveriesPercentage = calcPercentageDifference(now.getRecoveries(), before.getRecoveries());
        return diff;
    }

    private double calcPercentageDifference(int currentValue, int prevValue) {
        int diff = currentValue - prevValue;
        return Integer.valueOf(diff).doubleValue() / prevValue * 100;
    }

    private class StatsDifference {
        public double TotalPercentage;
        public int TotalDifference;
        public double DeathsPercentage;
        public int DeathsDifference;
        public double RecoveriesPercentage;
        public int RecoveriesDifference;
    }

    private String formatPercentage(double percentage) {
        return oneDigitPrecisionDecimalFormat.format(percentage);
    }

    private String formatInteger(int value) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        return numberFormat.format(value);
    }
}
