package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingResponseDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public BookingResponseDto create(Long userId, BookingDto bookingDto) {
        User booker = getUserOrThrow(userId);
        Item item = getItemOrThrow(bookingDto.getItemId());

        validateBookingCreation(booker, item, bookingDto);

        Booking booking = new Booking();
        booking.setStart(bookingDto.getStart());
        booking.setEnd(bookingDto.getEnd());
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(BookingStatus.WAITING);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Created booking with ID: {}", savedBooking.getId());

        return convertToResponseDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingResponseDto updateStatus(Long userId, Long bookingId, Boolean approved) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Only item owner can approve or reject booking");
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new BadRequestException("Booking status is already set");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updatedBooking = bookingRepository.save(booking);

        log.info("Updated booking status: bookingId={}, newStatus={}",
                bookingId, updatedBooking.getStatus());

        return convertToResponseDto(updatedBooking);
    }

    @Override
    public BookingResponseDto getById(Long userId, Long bookingId) {
        Booking booking = getBookingOrThrow(bookingId);

        if (!booking.getBooker().getId().equals(userId) &&
                !booking.getItem().getOwner().getId().equals(userId)) {
            throw new NotFoundException("Only booker or item owner can view booking");
        }

        return convertToResponseDto(booking);
    }

    @Override
    public List<BookingResponseDto> getAllByBooker(Long userId, BookingState state,
                                                   Integer from, Integer size) {
        getUserOrThrow(userId);

        PageRequest pageRequest = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "start"));

        Page<Booking> bookingsPage;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                bookingsPage = bookingRepository.findCurrentBookingsByBooker(userId, now, pageRequest);
                break;
            case PAST:
                bookingsPage = bookingRepository.findPastBookingsByBooker(userId, now, pageRequest);
                break;
            case FUTURE:
                bookingsPage = bookingRepository.findFutureBookingsByBooker(userId, now, pageRequest);
                break;
            case WAITING:
                bookingsPage = bookingRepository.findByBookerIdAndStatus(userId,
                        BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                bookingsPage = bookingRepository.findByBookerIdAndStatus(userId,
                        BookingStatus.REJECTED, pageRequest);
                break;
            case ALL:
            default:
                bookingsPage = bookingRepository.findByBookerId(userId, pageRequest);
        }

        return bookingsPage.getContent().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllByOwner(Long userId, BookingState state,
                                                  Integer from, Integer size) {
        getUserOrThrow(userId);

        PageRequest pageRequest = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "start"));

        Page<Booking> bookingsPage;
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case CURRENT:
                bookingsPage = bookingRepository.findCurrentBookingsByOwner(userId, now, pageRequest);
                break;
            case PAST:
                bookingsPage = bookingRepository.findPastBookingsByOwner(userId, now, pageRequest);
                break;
            case FUTURE:
                bookingsPage = bookingRepository.findFutureBookingsByOwner(userId, now, pageRequest);
                break;
            case WAITING:
                bookingsPage = bookingRepository.findByItemOwnerIdAndStatus(userId,
                        BookingStatus.WAITING, pageRequest);
                break;
            case REJECTED:
                bookingsPage = bookingRepository.findByItemOwnerIdAndStatus(userId,
                        BookingStatus.REJECTED, pageRequest);
                break;
            case ALL:
            default:
                bookingsPage = bookingRepository.findByItemOwnerId(userId, pageRequest);
        }

        return bookingsPage.getContent().stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getBookingsForItem(Long itemId) {
        return bookingRepository.findByItemIdOrderByStartDesc(itemId).stream()
                .map(this::convertToResponseDto)
                .collect(Collectors.toList());
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with ID: " + itemId));
    }

    private Booking getBookingOrThrow(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found with ID: " + bookingId));
    }

    private void validateBookingCreation(User booker, Item item, BookingDto bookingDto) {
        if (!item.getAvailable()) {
            throw new BadRequestException("Item is not available for booking");
        }

        if (booker.getId().equals(item.getOwner().getId())) {
            throw new NotFoundException("Owner cannot book their own item");
        }

        if (bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new BadRequestException("End date must be after start date");
        }

        if (bookingDto.getEnd().equals(bookingDto.getStart())) {
            throw new BadRequestException("Start and end dates cannot be equal");
        }
    }

    private BookingResponseDto convertToResponseDto(Booking booking) {
        BookingResponseDto dto = new BookingResponseDto();
        dto.setId(booking.getId());
        dto.setStart(booking.getStart());
        dto.setEnd(booking.getEnd());
        dto.setStatus(booking.getStatus());

        BookingResponseDto.BookerDto bookerDto = new BookingResponseDto.BookerDto();
        bookerDto.setId(booking.getBooker().getId());
        bookerDto.setName(booking.getBooker().getName());
        bookerDto.setEmail(booking.getBooker().getEmail());
        dto.setBooker(bookerDto);

        BookingResponseDto.BookingItemDto itemDto = new BookingResponseDto.BookingItemDto();
        itemDto.setId(booking.getItem().getId());
        itemDto.setName(booking.getItem().getName());
        itemDto.setDescription(booking.getItem().getDescription());
        itemDto.setAvailable(booking.getItem().getAvailable());
        dto.setItem(itemDto);

        return dto;
    }
}