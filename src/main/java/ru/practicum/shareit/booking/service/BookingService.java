package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;

import java.util.List;

public interface BookingService {

    BookingResponseDto create(Long userId, BookingDto bookingDto);

    BookingResponseDto updateStatus(Long userId, Long bookingId, Boolean approved);

    BookingResponseDto getById(Long userId, Long bookingId);

    List<BookingResponseDto> getAllByBooker(Long userId, BookingState state, Integer from, Integer size);

    List<BookingResponseDto> getAllByOwner(Long userId, BookingState state, Integer from, Integer size);

    List<BookingResponseDto> getBookingsForItem(Long itemId);
}