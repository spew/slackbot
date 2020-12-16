package org.poker.chess;

import org.junit.Test;
import org.poker.chess.player.Profile;
import org.poker.chess.player.Stats;

import static org.junit.Assert.assertNotNull;

public class PlayersTest {
    @Test
    public void getProfileShouldSucceed() {
        ChessClient chessClient = new ChessClient();
        Profile profile = chessClient.getPlayers().getProfile("deathdealer69");
        assertNotNull(profile);
    }

    @Test
    public void getStatsShouldSucceed() {
        ChessClient chessClient = new ChessClient();
        Stats stats = chessClient.getPlayers().getStats("tbstoodz");
        assertNotNull(stats);
    }
}
