package org.poker.youtube;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class YoutubeLiveBroadcastSearcher {
    private static final Logger logger = LoggerFactory.getLogger(YoutubeLiveBroadcastSearcher.class);

    private final YouTube youtube;
    private final String apiKey;

    public YoutubeLiveBroadcastSearcher(YouTube youtube, String apiKey) {
        this.youtube = youtube;
        this.apiKey = apiKey;
    }

    public SearchResult firstOrNull(String channelId) {
        try {
            return firstOrNullThrows(channelId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private SearchResult firstOrNullThrows(String channelId) throws IOException {
        YouTube.Search.List request = youtube.search().list("id,snippet")
                .setKey(apiKey)
                .setChannelId(channelId)
                .setType("video")
                .setEventType("live");
        SearchListResponse searchResponse = request.execute();
        if (searchResponse.getItems().size() > 0) {
            return searchResponse.getItems().get(0);
        }
        return null;
    }
}
