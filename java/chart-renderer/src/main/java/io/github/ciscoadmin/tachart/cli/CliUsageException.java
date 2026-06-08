package io.github.ciscoadmin.tachart.cli;

public final class CliUsageException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public CliUsageException(String message) {
        super(message);
    }

    public CliUsageException(String message, Throwable cause) {
        super(message, cause);
    }
}
