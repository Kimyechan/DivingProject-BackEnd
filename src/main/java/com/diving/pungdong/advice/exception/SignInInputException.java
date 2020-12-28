package com.diving.pungdong.advice.exception;

public class SignInInputException extends RuntimeException {
    public SignInInputException() {
    }

    public SignInInputException(String message) {
        super(message);
    }

    public SignInInputException(String message, Throwable cause) {
        super(message, cause);
    }
}
