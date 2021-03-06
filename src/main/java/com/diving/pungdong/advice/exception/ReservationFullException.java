package com.diving.pungdong.advice.exception;

public class ReservationFullException extends RuntimeException {
    public ReservationFullException() {
    }

    public ReservationFullException(String message) {
        super(message);
    }

    public ReservationFullException(String message, Throwable cause) {
        super(message, cause);
    }
}
