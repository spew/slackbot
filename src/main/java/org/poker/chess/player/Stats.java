package org.poker.chess.player;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Stats {
    @JsonProperty("chess_daily")
    private ChessStats daily;
    @JsonProperty("chess_blitz")
    private ChessStats blitz;
    @JsonProperty("chess_rapid")
    private ChessStats rapid;
    @JsonProperty("chess_bullet")
    private ChessStats bullet;
    @JsonProperty("fide")
    private int fide;
    @JsonProperty("tactics")
    private Tactics tactics;
    @JsonProperty("lessons")
    private Tactics lessons;
    @JsonProperty("puzzle_rush")
    private PuzzleRush puzzleRush;

    public ChessStats getDaily() {
        return daily;
    }

    public ChessStats getBlitz() {
        return blitz;
    }

    public ChessStats getRapid() {
        return rapid;
    }

    public ChessStats getBullet() {
        return bullet;
    }

    public int getFide() {
        return fide;
    }

    public Tactics getTactics() {
        return tactics;
    }

    public Tactics getLessons() {
        return lessons;
    }

    public PuzzleRush getPuzzleRush() {
        return puzzleRush;
    }
}