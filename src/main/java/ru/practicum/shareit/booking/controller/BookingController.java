package ru.practicum.shareit.booking.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/bookings")
public class BookingController {

    private final BookingService bookingService;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<BookingResponseDto> create(
            @RequestHeader(USER_HEADER) Long userId,
            @Valid @RequestBody BookingDto bookingDto) {
        log.info("Creating booking for user ID: {}, booking: {}", userId, bookingDto);
        BookingResponseDto createdBooking = bookingService.create(userId, bookingDto);
        return ResponseEntity.ok(createdBooking);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> updateStatus(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long bookingId,
            @RequestParam Boolean approved) {
        log.info("Updating booking status: bookingId={}, userId={}, approved={}",
                bookingId, userId, approved);
        BookingResponseDto updatedBooking = bookingService.updateStatus(userId, bookingId, approved);
        return ResponseEntity.ok(updatedBooking);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponseDto> getById(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long bookingId) {
        log.info("Getting booking by ID: bookingId={}, userId={}", bookingId, userId);
        BookingResponseDto booking = bookingService.getById(userId, bookingId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllByBooker(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting bookings for booker: userId={}, state={}, from={}, size={}",
                userId, state, from, size);
        List<BookingResponseDto> bookings = bookingService.getAllByBooker(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponseDto>> getAllByOwner(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(defaultValue = "ALL") BookingState state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting bookings for owner: userId={}, state={}, from={}, size={}",
                userId, state, from, size);
        List<BookingResponseDto> bookings = bookingService.getAllByOwner(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }
}