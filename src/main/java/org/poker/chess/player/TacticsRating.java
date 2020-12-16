package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TacticsRating {
    @JsonProperty("rating")
    private int rating;
    @JsonProperty("date")
    private long date;

    public int getRating() {
        return rating;
    }

    public long getDate() {
        return date;
    }
}
