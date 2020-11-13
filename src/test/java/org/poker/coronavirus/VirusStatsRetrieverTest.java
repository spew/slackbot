package org.poker.coronavirus;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class VirusStatsRetrieverTest {
    @Test
    public void retrieveShouldSucceed() {
        VirusStatsRetriever retriever = new VirusStatsRetriever();
        VirusStats stats = retriever.retrieve();
        assertNotNull(stats);
        stats = retriever.retrieve("us");
        assertNotNull(stats);
    }
}
