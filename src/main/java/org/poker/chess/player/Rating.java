package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Rating {
    @JsonProperty("rating")
    private int rating;
    @JsonProperty("date")
    private long date;
    @JsonProperty("rd")
    private int rd;
    @JsonProperty("game")
    private String game;

    public int getRating() {
        return rating;
    }

    public long getDate() {
        return date;
    }

    public int getRd() {
        return rd;
    }

    public String getGame() {
        return game;
    }
}
