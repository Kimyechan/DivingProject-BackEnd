package com.diving.pungdong.advice.exception;

public class EmailDuplicationException extends RuntimeException {
    public EmailDuplicationException() {
    }

    public EmailDuplicationException(String message) {
        super(message);
    }

    public EmailDuplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
