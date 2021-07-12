package com.diving.pungdong.advice.exception;

public class ClosedLectureException extends RuntimeException {
    public ClosedLectureException() {
    }

    public ClosedLectureException(String message) {
        super(message);
    }

    public ClosedLectureException(String message, Throwable cause) {
        super(message, cause);
    }
}
