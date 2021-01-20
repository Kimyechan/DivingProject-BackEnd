package com.diving.pungdong.advice.exception;

public class NoPermissionsException extends RuntimeException {
    public NoPermissionsException() {
    }

    public NoPermissionsException(String message) {
        super(message);
    }

    public NoPermissionsException(String message, Throwable cause) {
        super(message, cause);
    }
}
