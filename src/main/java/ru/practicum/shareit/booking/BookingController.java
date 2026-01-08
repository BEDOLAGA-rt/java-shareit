package ru.practicum.shareit.booking;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public Booking create(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody Booking booking) {
        return bookingService.create(userId, booking);
    }

    @PatchMapping("/{bookingId}")
    public Booking approve(@RequestHeader("X-Sharer-User-Id") Long userId,
                           @PathVariable Long bookingId,
                           @RequestParam boolean approved) {
        return bookingService.approve(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public Booking get(@RequestHeader("X-Sharer-User-Id") Long userId,
                       @PathVariable Long bookingId) {
        return bookingService.get(userId, bookingId);
    }

    @GetMapping
    public List<Booking> getByBooker(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return bookingService.getByBooker(userId);
    }
}