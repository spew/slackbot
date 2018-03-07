package org.poker;

public class TerminalErrorException extends RuntimeException {
    public TerminalErrorException(String message) {
        super(message);
    }

    public TerminalErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
