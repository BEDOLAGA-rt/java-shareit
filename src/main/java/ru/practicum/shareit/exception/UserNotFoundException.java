package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends NotFoundException {
    public UserNotFoundException(Long userId) {
        super(String.format("Пользователь с ID %d не найден", userId));
    }

    public UserNotFoundException(String email) {
        super(String.format("Пользователь с email %s не найден", email));
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}