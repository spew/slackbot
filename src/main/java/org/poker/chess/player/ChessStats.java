package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChessStats {
    @JsonProperty("last")
    private Rating last;
    @JsonProperty("best")
    private Rating best;
    @JsonProperty("record")
    private Record record;

    public Rating getLast() {
        return last;
    }

    public Rating getBest() {
        return best;
    }

    public Record getRecord() {
        return record;
    }
}
