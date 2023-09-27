package com.fuzzysound.walrus.common.exception;

public class NoWalletException extends RuntimeException {
    public NoWalletException() {
    }

    public NoWalletException(String message) {
        super(message);
    }

    public NoWalletException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoWalletException(Throwable cause) {
        super(cause);
    }

    public NoWalletException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
