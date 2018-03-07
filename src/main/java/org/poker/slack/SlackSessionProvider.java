package org.poker.slack;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.poker.config.ApplicationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SlackSessionProvider {
    private static final Logger logger = LoggerFactory.getLogger(SlackSessionProvider.class);
    private final ApplicationConfiguration applicationConfiguration;

    public SlackSessionProvider(ApplicationConfiguration applicationConfiguration) {
        this.applicationConfiguration = applicationConfiguration;
    }

    public SlackSession provide() {
        String apiToken = applicationConfiguration.getSlackApiToken();
        new SlackApiTokenValidator().validate(apiToken);
        SlackSession session = SlackSessionFactory.createWebSocketSlackSession(apiToken);
        connect(session);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Executing slack shutdown hook.");
            try {
                session.disconnect();
                logger.info("Disconnected from slack.");
            } catch (Exception e) {
                logger.error("Error disconnecting from slack session", e);
            }
        }));
        return session;
    }

    private void connect(SlackSession slackSession) {
        try {
            slackSession.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
