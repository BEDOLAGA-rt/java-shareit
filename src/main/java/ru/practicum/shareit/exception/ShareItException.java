package ru.practicum.shareit.exception;

public abstract class ShareItException extends RuntimeException {
    public ShareItException(String message) {
        super(message);
    }

    public ShareItException(String message, Throwable cause) {
        super(message, cause);
    }
}