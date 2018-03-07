package org.poker;

import com.ullink.slack.simpleslackapi.SlackSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.poker.listener.DefaultListenerAdder;
import org.poker.slack.SlackSessionProvider;

public class SlackBotApplication {
    private static final Logger logger = LogManager.getLogger(SlackBotApplication.class);
    private ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

    public void run() {
        logger.info("Starting slackbot...");
        SlackSession session = new SlackSessionProvider(applicationConfiguration).provide();
        new DefaultListenerAdder(applicationConfiguration).addMessagePostedListeners(session);
        runLoop();
        logger.info("Exiting slackbot.");
    }

    private void runLoop() {
        try {
            while (true) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            logger.info("Interrupted and shutting down.");
        }
    }
}
