package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PuzzleRush {
    @JsonProperty("best")
    private Rush best;

    public Rush getBest() {
        return best;
    }
}
