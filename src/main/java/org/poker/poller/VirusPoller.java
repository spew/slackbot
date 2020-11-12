package org.poker.poller;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import humanize.ICUHumanize;
import org.poker.coronavirus.VirusStats;
import org.poker.coronavirus.VirusStatsRetriever;
import org.poker.youtube.YoutubeLiveBroadcastSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class VirusPoller implements Poller {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeLiveBroadcastSearcher.class);
    private static final DecimalFormat oneDigitPrecisionDecimalFormat = new DecimalFormat("#,##0.0");

    private Date anchorTime;
    private VirusStats anchorValue;
    private final VirusStatsRetriever statsRetriever;
    private final PollerSlackMessageSink messageSink;

    public VirusPoller(VirusStatsRetriever retriever, PollerSlackMessageSink messageSink) {
        this.statsRetriever = retriever;
        this.messageSink = messageSink;
    }

    @Override
    public void Poll() {
        VirusStats stats = statsRetriever.retrieve();
        if (anchorValue == null) {
            anchorValue = stats;
            anchorTime = new Date();
        }
        StatsDifference diff = getStatsDifference(anchorValue, stats);
        if (!shouldPrint(diff)) {
            return;
        }
        Date newTime = new Date();
        SlackAttachment attachment = formatAttachment(newTime, anchorTime, stats, diff);
        messageSink.SendMessage(attachment);
        anchorValue = stats;
        anchorTime = newTime;
    }

    @Override
    public TimeUnit getIntervalTimeUnit() {
        return TimeUnit.MINUTES;
    }

    @Override
    public long getIntervalValue() {
        return 10;
    }

    private SlackAttachment formatAttachment(Date curDate, Date prevDate, VirusStats currentStats, StatsDifference diff) {
        SlackAttachment attachment = new SlackAttachment();
        String naturalTime = ICUHumanize.naturalTime(prevDate, curDate, Locale.US);
        naturalTime = naturalTime.replace("from now", "ago");
        attachment.setFallback(String.format("The virus is spreading! Relative to %s:", naturalTime));
        addField(attachment, "Cases", currentStats.getTotalCases(), diff.TotalDifference, diff.TotalPercentage);
        addField(attachment, "Deaths", currentStats.getDeaths(), diff.DeathsDifference, diff.DeathsPercentage);
        return attachment;
    }

    private void addField(SlackAttachment attachment, String title, int curValue, int diff, double percentageDiff) {
        attachment.addField(title,
                String.format("+%s (+%s%%)", diff, formatPercentage(percentageDiff)),
                true
        );
    }

    private boolean shouldPrint(StatsDifference diff) {
        if (diff.TotalPercentage >= 5.0) {
            return true;
        }
        if (diff.DeathsDifference >= 5.0) {
            return true;
        }
        return false;
    }

    private StatsDifference getStatsDifference(VirusStats before, VirusStats now) {
        StatsDifference diff = new StatsDifference();
        diff.TotalDifference = now.getTotalCases() - before.getTotalCases();
        diff.DeathsDifference = now.getDeaths() - before.getDeaths();
        diff.RecoveriesDifference = now.getRecoveries() - before.getRecoveries();
        diff.TotalPercentage = calcPercentageDifference(now.getTotalCases(), before.getTotalCases());
        diff.TotalPercentage = calcPercentageDifference(now.getDeaths(), before.getDeaths());
        diff.TotalPercentage = calcPercentageDifference(now.getRecoveries(), before.getRecoveries());
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
}
