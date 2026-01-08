package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingService {

    Booking create(Long userId, Booking booking);

    Booking approve(Long userId, Long bookingId, boolean approved);

    Booking get(Long userId, Long bookingId);

    List<Booking> getByBooker(Long userId);
}