package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.poker.stock.LogoURLRetriever;
import org.poker.stock.YahooFinanceStockResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class StockQuoteMessageListenerTest {
    @Test
    public void amznTicker() {
        assertTickers("$amzn", Arrays.asList("Amazon.com, Inc."));
    }

    @Test
    public void brkTicker() {
        assertTickers("$brk.a", Arrays.asList("Berkshire Hathaway Inc."));
    }

    @Test
    public void djiTicker() {
        assertTickers("$^dji", Arrays.asList("Dow Jones Industrial Average"));
    }

    @Test
    public void multipleTickers() {
        assertTickers("$amzn $goog", Arrays.asList("Amazon.com, Inc.", "Alphabet Inc."));
    }

    private void assertTickers(String message, List<String> expectedTitles) {
        LogoURLRetriever logoURLRetriever = mock(LogoURLRetriever.class);
        StockQuoteMessageListener listener = new StockQuoteMessageListener(new YahooFinanceStockResolver(), logoURLRetriever);
        SlackMessagePosted event = mock(SlackMessagePosted.class);
        when(event.getMessageContent()).thenReturn(message);
        SlackSession session = mock(SlackSession.class);
        listener.onEvent(event, session);
        ArgumentCaptor<SlackPreparedMessage> argumentCaptor = ArgumentCaptor.forClass(SlackPreparedMessage.class);
        verify(session, times(1)).sendMessage(any(SlackChannel.class), argumentCaptor.capture());
        SlackPreparedMessage actualMessage = argumentCaptor.getValue();
        List<SlackAttachment> attachments = new ArrayList<>();
        for (String t : expectedTitles) {
            SlackAttachment expected = new SlackAttachment();
            expected.addField(t, "", false);
            attachments.add(expected);
        }
        assertSlackPreparedMessage(attachments, actualMessage);
    }

    private void assertSlackPreparedMessage(List<SlackAttachment> expectedAttachments, SlackPreparedMessage actualMessage) {
        assertNotNull(actualMessage.getAttachments());
        assertEquals(actualMessage.getAttachments().size(), expectedAttachments.size());
        for (int i = 0; i < actualMessage.getAttachments().size(); i++) {
            assertTickerAttachment(expectedAttachments.get(i), actualMessage.getAttachments().get(i));
        }
    }

    private void assertTickerAttachment(SlackAttachment expected, SlackAttachment actual) {
        assertEquals(1, actual.getFields().size());
        assertEquals(expected.getFields().get(0).getTitle(), actual.getFields().get(0).getTitle());
    }
}
