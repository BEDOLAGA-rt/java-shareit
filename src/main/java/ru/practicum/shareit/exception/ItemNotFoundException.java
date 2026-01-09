package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ItemNotFoundException extends NotFoundException {
    public ItemNotFoundException(Long itemId) {
        super(String.format("Вещь с ID %d не найдена", itemId));
    }

    public ItemNotFoundException(String message) {
        super(message);
    }
}