package com.fuzzysound.walrus.common.exception;

public class Web3Exception extends RuntimeException {
    public Web3Exception() {
    }

    public Web3Exception(String message) {
        super(message);
    }

    public Web3Exception(String message, Throwable cause) {
        super(message, cause);
    }

    public Web3Exception(Throwable cause) {
        super(cause);
    }

    public Web3Exception(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
