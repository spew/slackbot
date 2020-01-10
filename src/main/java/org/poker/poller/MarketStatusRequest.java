package org.poker.poller;

import com.fasterxml.jackson.databind.JsonNode;
import yahoofinance.quotes.query1v7.QuotesRequest;

public class MarketStatusRequest extends QuotesRequest<MarketStatusRequest.MarketStatus> {

    public MarketStatusRequest() {
        /* I didn't find any functionality for retrieving general market info, so this hack
         * sends a normal request for a $goog quote (any valid ticker would be fine), but
         * all we parse from the response is the market status. */
        super("goog");
    }

    @Override
    protected MarketStatus parseJson(JsonNode node) {
        String marketState = extractMarketState(node);
        // Could also check for PRE, POST, etc here, but imo that'd be too noisy.
        if (null != marketState && marketState.equalsIgnoreCase("REGULAR")) {
            return MarketStatus.OPEN;
        }
        return MarketStatus.CLOSED;
    }

    private String extractMarketState(JsonNode node) {
        if(node.has("marketState")) {
            return node.get("marketState").asText();
        }
        return null;
    }

    enum MarketStatus {
        OPEN,
        CLOSED
    }
}
