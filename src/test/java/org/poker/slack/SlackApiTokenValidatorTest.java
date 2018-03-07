package org.poker.slack;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.poker.TerminalErrorException;

public class SlackApiTokenValidatorTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void nullShouldThrow() {
        expectedException.expect(TerminalErrorException.class);
        expectedException.expectMessage("Slack API Token cannot be null or empty.");
        new SlackApiTokenValidator().validate(null);
    }

    @Test
    public void emptyShouldThrow() {
        expectedException.expect(TerminalErrorException.class);
        expectedException.expectMessage("Slack API Token cannot be null or empty.");
        new SlackApiTokenValidator().validate("");
    }
}
