package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateEmailException extends ConflictException {
    public DuplicateEmailException(String email) {
        super(String.format("Пользователь с email %s уже существует", email));
    }
}