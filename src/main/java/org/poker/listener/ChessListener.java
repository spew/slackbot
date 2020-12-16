package org.poker.listener;

import com.google.common.base.Joiner;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import org.poker.chess.ChessClient;
import org.poker.chess.player.Stats;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessListener implements SlackMessagePostedListener {
    private ChessClient chessClient = new ChessClient();
    private List<String> playersList = Arrays.asList("deathdealer69", "tbstoodz", "mylons", "idletom", "m4ttj0nes", "p4labduhl");

    public ChessListener() {
        playersList.sort(String::compareToIgnoreCase);
    }

    public void onEvent(SlackMessagePosted event, SlackSession session) {
        if (!shouldHandle(event.getMessageContent())) {
            return;
        }
        Map<String, Stats> playerToStats = getStats();
        SlackPreparedMessage message = formatMessage(playerToStats);
        session.sendMessage(event.getChannel(), message);
    }

    private Map<String, Stats> getStats() {
        Map<String, Stats> playerToStats = new HashMap<>();
        for (String p : playersList) {
            Stats stats = chessClient.getPlayers().getStats(p);
            playerToStats.put(p, stats);
        }
        return playerToStats;
    }

    private SlackPreparedMessage formatMessage(Map<String, Stats> playerToStats) {
        List<SlackAttachment> attachments = new ArrayList<>();
        for (String p : playersList) {
            Stats stats = playerToStats.get(p);
            attachments.add(formatProfileAttachment(p, stats));
        }
        SlackPreparedMessage preparedMessage = new SlackPreparedMessage.Builder()
                .withMessage("Chess Ratings")
                .withAttachments(attachments)
                .build();
        return preparedMessage;
    }

    private SlackAttachment formatProfileAttachment(String playerName, Stats stats) {
        SlackAttachment attachment = new SlackAttachment();
        List<String> ratings = new ArrayList<>();
        if (stats.getRapid() != null) {
            ratings.add(String.format("%d rapid", stats.getRapid().getLast().getRating()));
        }
        if (stats.getBlitz() != null) {
            ratings.add(String.format("%d blitz", stats.getBlitz().getLast().getRating()));
        }
        if (stats.getBullet() != null) {
            ratings.add(String.format("%d bullet", stats.getBullet().getLast().getRating()));
        }
        String message = String.format("%s: %s", playerName, Joiner.on(" | ").join(ratings));
        attachment.setText(message);
        return attachment;
    }

    private boolean shouldHandle(String message) {
        return message.startsWith(".chess");
    }
}
