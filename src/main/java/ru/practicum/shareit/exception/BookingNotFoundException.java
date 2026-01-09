package ru.practicum.shareit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class BookingNotFoundException extends NotFoundException {
    public BookingNotFoundException(Long bookingId) {
        super(String.format("Бронирование с ID %d не найдено", bookingId));
    }

    public BookingNotFoundException(String message) {
        super(message);
    }
}