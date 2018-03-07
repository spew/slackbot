package org.poker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        int returnCode = 0;
        try {
            SlackBotApplication slackBotApplication = new SlackBotApplication();
            slackBotApplication.run();
        } catch (TerminalErrorException e) {
            logger.info(e.getMessage());
            returnCode = 1;
        } catch (Exception e) {
            logger.error("Unhandled exception in SlackBotApplication", e);
            throw e;
        }
        System.exit(returnCode);
    }
}
