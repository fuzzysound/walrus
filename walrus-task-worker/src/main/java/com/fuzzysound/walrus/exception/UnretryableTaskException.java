package com.fuzzysound.walrus.exception;

public class UnretryableTaskException extends RuntimeException {
    public UnretryableTaskException() {
    }

    public UnretryableTaskException(String message) {
        super(message);
    }

    public UnretryableTaskException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnretryableTaskException(Throwable cause) {
        super(cause);
    }

    public UnretryableTaskException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
