package ru.practicum.shareit.booking.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    public BookingServiceImpl(BookingRepository bookingRepository,
                              ItemRepository itemRepository) {
        this.bookingRepository = bookingRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Booking create(Long userId, Booking booking) {
        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Item not found"));

        if (!item.getAvailable()) {
            throw new IllegalStateException("Item not available");
        }

        if (item.getOwnerId().equals(userId)) {
            throw new NoSuchElementException("Owner cannot book own item");
        }

        booking.setBookerId(userId);
        booking.setStatus(BookingStatus.WAITING);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking approve(Long userId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        Item item = itemRepository.findById(booking.getItemId())
                .orElseThrow(() -> new NoSuchElementException("Item not found"));

        if (!item.getOwnerId().equals(userId)) {
            throw new NoSuchElementException("Not owner");
        }

        booking.setStatus(approved
                ? BookingStatus.APPROVED
                : BookingStatus.REJECTED);

        return bookingRepository.save(booking);
    }

    @Override
    public Booking get(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NoSuchElementException("Booking not found"));

        if (!booking.getBookerId().equals(userId)) {
            Item item = itemRepository.findById(booking.getItemId())
                    .orElseThrow(() -> new NoSuchElementException("Item not found"));

            if (!item.getOwnerId().equals(userId)) {
                throw new NoSuchElementException("Access denied");
            }
        }

        return booking;
    }

    @Override
    public List<Booking> getByBooker(Long userId) {
        return bookingRepository.findByBookerIdOrderByStartDesc(userId);
    }
}