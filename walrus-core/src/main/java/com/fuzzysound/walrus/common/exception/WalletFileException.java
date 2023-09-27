package com.fuzzysound.walrus.common.exception;

public class WalletFileException extends RuntimeException {
    public WalletFileException() {
    }

    public WalletFileException(String message) {
        super(message);
    }

    public WalletFileException(String message, Throwable cause) {
        super(message, cause);
    }

    public WalletFileException(Throwable cause) {
        super(cause);
    }

    public WalletFileException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
