package com.diving.pungdong.advice.exception;

public class ForbiddenTokenException extends RuntimeException {
    public ForbiddenTokenException() {
    }

    public ForbiddenTokenException(String message) {
        super(message);
    }

    public ForbiddenTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
