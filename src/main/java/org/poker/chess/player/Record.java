package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Record {
    @JsonProperty("win")
    private int win;
    @JsonProperty("loss")
    private int loss;
    @JsonProperty("draw")
    private int draw;
    @JsonProperty("time_per_move")
    private long timePerMove;
    @JsonProperty("timeout_percent")
    private long timeoutPercent;

    public int getWin() {
        return win;
    }

    public int getLoss() {
        return loss;
    }

    public int getDraw() {
        return draw;
    }

    public long getTimePerMove() {
        return timePerMove;
    }

    public long getTimeoutPercent() {
        return timeoutPercent;
    }
}
