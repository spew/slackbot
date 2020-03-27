package org.poker.poller;

import com.google.api.services.youtube.model.SearchResult;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import org.poker.youtube.YoutubeLiveBroadcastSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class YoutubeChannelPoller implements Poller {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeChannelPoller.class);

    private final String youtubeChannelId;
    private final PollerSlackMessageSink messageSink;
    private final YoutubeLiveBroadcastSearcher liveBroadcastSearcher;
    private SearchResult previousResult;

    public YoutubeChannelPoller(String youtubeChannelId, YoutubeLiveBroadcastSearcher liveBroadcastSearcher, PollerSlackMessageSink messageSink) {
        this.youtubeChannelId = youtubeChannelId;
        this.messageSink = messageSink;
        this.liveBroadcastSearcher = liveBroadcastSearcher;
    }

    @Override
    public TimeUnit getIntervalTimeUnit() {
        return TimeUnit.SECONDS;
    }

    @Override
    public long getIntervalValue() {
        return TimeUnit.MINUTES.toSeconds(2) + 30;
    }

    @Override
    public void Poll() {
        SearchResult searchResult = liveBroadcastSearcher.firstOrNull(youtubeChannelId);
        if (isNewResult(searchResult)) {
            messageSink.SendMessage(formatMessage(searchResult));
        }
    }

    private boolean isNewResult(SearchResult searchResult) {
        if (searchResult == null) {
            return false;
        }
        if (previousResult != null) {
            if (previousResult.getSnippet().getPublishedAt().equals(searchResult.getSnippet().getPublishedAt())) {
                return false;
            }
        }
        previousResult = searchResult;
        return true;
    }

    private SlackAttachment formatMessage(SearchResult searchResult) {
        SlackAttachment attachment = new SlackAttachment();
        attachment.setThumbUrl(searchResult.getSnippet().getThumbnails().getDefault().getUrl());
        attachment.setColor("danger");
        String url = getUrl(searchResult);
        attachment.setTitle(searchResult.getSnippet().getTitle());
        attachment.setTitleLink(url);
        attachment.setText(searchResult.getSnippet().getDescription());
        return attachment;
    }

    private String getUrl(SearchResult searchResult) {
        return String.format("https://www.youtube.com/watch?v=%s", searchResult.getId().getVideoId());
    }
}
