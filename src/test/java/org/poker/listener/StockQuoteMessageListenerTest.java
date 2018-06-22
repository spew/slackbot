package org.poker.listener;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.poker.stock.YahooFinanceStockResolver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class StockQuoteMessageListenerTest {
    @Test
    public void longMessagesShouldBeIgnored() {
        StockQuoteMessageListener listener = new StockQuoteMessageListener(new YahooFinanceStockResolver());
        SlackMessagePosted event = mock(SlackMessagePosted.class);
        when(event.getMessageContent()).thenReturn("$thisisreallylongandshouldbeignored");
        SlackSession session = mock(SlackSession.class);
        listener.onEvent(event, session);
        verify(session, never()).sendMessage(any(), any(), any(SlackAttachment.class));
    }

    @Test
    public void amznTicker() {
        StockQuoteMessageListener listener = new StockQuoteMessageListener(new YahooFinanceStockResolver());
        SlackMessagePosted event = mock(SlackMessagePosted.class);
        when(event.getMessageContent()).thenReturn("$amzn");
        SlackSession session = mock(SlackSession.class);
        listener.onEvent(event, session);
        ArgumentCaptor<SlackAttachment> argumentCaptor = ArgumentCaptor.forClass(SlackAttachment.class);
        verify(session, times(1)).sendMessage(any(), any(String.class), argumentCaptor.capture());
        SlackAttachment actualAttachment = argumentCaptor.getValue();
        assertEquals(1, actualAttachment.getFields().size());
        assertEquals("Amazon.com, Inc.", actualAttachment.getFields().get(0).getTitle());
    }

    @Test
    public void brkTicker() {
        StockQuoteMessageListener listener = new StockQuoteMessageListener(new YahooFinanceStockResolver());
        SlackMessagePosted event = mock(SlackMessagePosted.class);
        when(event.getMessageContent()).thenReturn("$brk.a");
        SlackSession session = mock(SlackSession.class);
        listener.onEvent(event, session);
        ArgumentCaptor<SlackAttachment> argumentCaptor = ArgumentCaptor.forClass(SlackAttachment.class);
        verify(session, times(1)).sendMessage(any(), any(String.class), argumentCaptor.capture());
        SlackAttachment actualAttachment = argumentCaptor.getValue();
        assertEquals(1, actualAttachment.getFields().size());
        assertEquals("Berkshire Hathaway Inc.", actualAttachment.getFields().get(0).getTitle());
    }

    @Test
    public void djiTicker() {
        StockQuoteMessageListener listener = new StockQuoteMessageListener(new YahooFinanceStockResolver());
        SlackMessagePosted event = mock(SlackMessagePosted.class);
        when(event.getMessageContent()).thenReturn("$^dji");
        SlackSession session = mock(SlackSession.class);
        listener.onEvent(event, session);
        ArgumentCaptor<SlackAttachment> argumentCaptor = ArgumentCaptor.forClass(SlackAttachment.class);
        verify(session, times(1)).sendMessage(any(), any(String.class), argumentCaptor.capture());
        SlackAttachment actualAttachment = argumentCaptor.getValue();
        assertEquals(1, actualAttachment.getFields().size());
        assertEquals("Dow Jones Industrial Average", actualAttachment.getFields().get(0).getTitle());
    }
}
