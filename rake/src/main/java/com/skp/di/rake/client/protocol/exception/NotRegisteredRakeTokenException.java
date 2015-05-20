package com.skp.di.rake.client.protocol.exception;

public class NotRegisteredRakeTokenException extends RakeException {
    private static final long serialVersinoUID = 0;
    private Throwable cause;

    static public final int ERROR_CODE = 40101;

    public NotRegisteredRakeTokenException(String message) {
        super(message);
    }

    public NotRegisteredRakeTokenException(Throwable cause) {
        super(cause.getMessage());
        this.cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this.cause;
    }
}
