package org.poker.chess;

public class ChessClient {
    private final String baseURL = "https://api.chess.com/pub";
    private Players players = new Players(baseURL);

    public Players getPlayers() {
        return players;
    }
}
