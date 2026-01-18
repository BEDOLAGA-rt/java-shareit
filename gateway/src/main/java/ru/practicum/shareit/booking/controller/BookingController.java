package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.client.BookingClient;
import ru.practicum.shareit.booking.dto.BookingRequestDto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
	private final BookingClient bookingClient;

	@PostMapping
	public ResponseEntity<Object> createBooking(
			@RequestHeader("X-Sharer-User-Id") Long userId,
			@Valid @RequestBody BookingRequestDto bookingRequestDto) {
		log.info("Creating booking by user {}: {}", userId, bookingRequestDto);
		return bookingClient.createBooking(userId, bookingRequestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> approveBooking(
			@RequestHeader("X-Sharer-User-Id") Long userId,
			@PathVariable Long bookingId,
			@RequestParam Boolean approved) {
		log.info("Approving booking {} by user {}: approved={}", bookingId, userId, approved);
		return bookingClient.approveBooking(userId, bookingId, approved);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> getBookingById(
			@RequestHeader("X-Sharer-User-Id") Long userId,
			@PathVariable Long bookingId) {
		log.info("Getting booking {} by user {}", bookingId, userId);
		return bookingClient.getBookingById(userId, bookingId);
	}

	@GetMapping
	public ResponseEntity<Object> getUserBookings(
			@RequestHeader("X-Sharer-User-Id") Long userId,
			@RequestParam(defaultValue = "ALL") String state,
			@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
			@Positive @RequestParam(defaultValue = "10") Integer size) {
		log.info("Getting bookings for user {} with state {} from {} size {}", userId, state, from, size);
		return bookingClient.getUserBookings(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getOwnerBookings(
			@RequestHeader("X-Sharer-User-Id") Long userId,
			@RequestParam(defaultValue = "ALL") String state,
			@PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
			@Positive @RequestParam(defaultValue = "10") Integer size) {
		log.info("Getting owner bookings for user {} with state {} from {} size {}", userId, state, from, size);
		return bookingClient.getOwnerBookings(userId, state, from, size);
	}
}