package com.fuzzysound.walrus.common.exception;

public class AuthorizationFailedException extends RuntimeException {
    public AuthorizationFailedException() {
    }

    public AuthorizationFailedException(String message) {
        super(message);
    }

    public AuthorizationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorizationFailedException(Throwable cause) {
        super(cause);
    }

    public AuthorizationFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
