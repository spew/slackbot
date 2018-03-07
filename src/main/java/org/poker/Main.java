package org.poker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        try {
            SlackBotApplication slackBotApplication = new SlackBotApplication();
            slackBotApplication.run();
        } catch (Exception e) {
            logger.error("Unhandled exception in SlackBotApplication", e);
            throw e;
        }
    }
}
