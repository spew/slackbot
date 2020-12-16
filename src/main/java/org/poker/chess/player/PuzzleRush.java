package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PuzzleRush {
    @JsonProperty("best")
    private Rush best;
    @JsonProperty("daily")
    private Rush daily;

    public Rush getBest() {
        return best;
    }
}
