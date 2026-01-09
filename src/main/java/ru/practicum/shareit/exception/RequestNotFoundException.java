package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RequestNotFoundException extends NotFoundException {
    public RequestNotFoundException(Long requestId) {
        super(String.format("Запрос с ID %d не найден", requestId));
    }

    public RequestNotFoundException(String message) {
        super(message);
    }
}