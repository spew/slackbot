package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Rush {
    @JsonProperty("total_attempts")
    private int totalAttempts;
    @JsonProperty("score")
    private int score;
}
