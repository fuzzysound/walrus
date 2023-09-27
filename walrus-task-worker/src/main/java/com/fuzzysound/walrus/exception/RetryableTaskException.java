package com.fuzzysound.walrus.exception;

public class RetryableTaskException extends RuntimeException {
    public RetryableTaskException() {
    }

    public RetryableTaskException(String message) {
        super(message);
    }

    public RetryableTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public RetryableTaskException(Throwable cause) {
        super(cause);
    }

    public RetryableTaskException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
