package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.List;

public interface BookingService {

    /**
     * Создание нового бронирования
     */
    BookingDto createBooking(BookingCreateDto bookingCreateDto, Long userId);

    /**
     * Подтверждение или отклонение бронирования владельцем
     */
    BookingDto approveBooking(Long bookingId, Long userId, Boolean approved);

    /**
     * Получение бронирования по ID
     */
    BookingDto getBookingById(Long bookingId, Long userId);

    /**
     * Получение всех бронирований пользователя
     */
    List<BookingDto> getUserBookings(Long userId, String state, Integer from, Integer size);

    /**
     * Получение всех бронирований вещей пользователя
     */
    List<BookingDto> getOwnerBookings(Long userId, String state, Integer from, Integer size);

    /**
     * Проверка, может ли пользователь оставить отзыв на вещь
     */
    boolean canUserCommentItem(Long userId, Long itemId);

    BookingShortDto getLastBookingForItem(Long itemId);

    /**
     * Получение следующего бронирования вещи
     */
    BookingShortDto getNextBookingForItem(Long itemId);
}