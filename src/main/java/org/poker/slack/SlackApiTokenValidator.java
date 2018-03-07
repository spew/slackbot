package org.poker.slack;

import com.google.common.base.Strings;
import org.poker.TerminalErrorException;

public class SlackApiTokenValidator {
    public void validate(String apiToken) {
        if (Strings.isNullOrEmpty(apiToken)) {
            throw new TerminalErrorException("Slack API Token cannot be null or empty.");
        }
    }
}
