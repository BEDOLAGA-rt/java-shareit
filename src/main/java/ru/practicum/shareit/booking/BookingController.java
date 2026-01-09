package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookingDto> createBooking(
            @Valid @RequestBody BookingCreateDto bookingCreateDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("POST /bookings - создание бронирования пользователем {}", userId);
        BookingDto bookingDto = bookingService.createBooking(bookingCreateDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<BookingDto> approveBooking(
            @PathVariable Long bookingId,
            @RequestParam Boolean approved,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("PATCH /bookings/{} - подтверждение бронирования пользователем {}",
                bookingId, userId);
        BookingDto bookingDto = bookingService.approveBooking(bookingId, userId, approved);
        return ResponseEntity.ok(bookingDto);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(
            @PathVariable Long bookingId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("GET /bookings/{} - получение бронирования пользователем {}",
                bookingId, userId);
        BookingDto bookingDto = bookingService.getBookingById(bookingId, userId);
        return ResponseEntity.ok(bookingDto);
    }

    @GetMapping
    public ResponseEntity<List<BookingDto>> getUserBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /bookings?state={}&from={}&size={} - бронирования пользователя {}",
                state, from, size, userId);
        List<BookingDto> bookings = bookingService.getUserBookings(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingDto>> getOwnerBookings(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "ALL") String state,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /bookings/owner?state={}&from={}&size={} - бронирования владельца {}",
                state, from, size, userId);
        List<BookingDto> bookings = bookingService.getOwnerBookings(userId, state, from, size);
        return ResponseEntity.ok(bookings);
    }
}