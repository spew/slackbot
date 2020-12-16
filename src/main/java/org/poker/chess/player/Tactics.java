package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Tactics {
    @JsonProperty("highest")
    private TacticsRating highest;
    @JsonProperty("lowest")
    private TacticsRating lowest;

    public TacticsRating getHighest() {
        return highest;
    }

    public TacticsRating getLowest() {
        return lowest;
    }
}
