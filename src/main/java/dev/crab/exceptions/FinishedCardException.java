package dev.crab.exceptions;

public class FinishedCardException extends RuntimeException {
    public FinishedCardException(String message) {
        super(message);
    }
}
