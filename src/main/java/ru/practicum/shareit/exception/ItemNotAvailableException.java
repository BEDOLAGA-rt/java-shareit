package ru.practicum.shareit.exception;

public class ItemNotAvailableException extends ValidationException {
    public ItemNotAvailableException(String message) {
        super(message);
    }
}