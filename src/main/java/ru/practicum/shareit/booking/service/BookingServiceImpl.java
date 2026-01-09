package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.dto.BookingCreateDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    @Transactional
    public BookingDto createBooking(BookingCreateDto bookingCreateDto, Long userId) {
        log.info("Создание бронирования пользователем {}: {}", userId, bookingCreateDto);

        // Проверяем существование пользователя
        User booker = getUser(userId);

        // Проверяем существование вещи
        Item item = getItem(bookingCreateDto.getItemId());

        // Проверяем, что пользователь не владелец вещи
        if (item.getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Владелец не может бронировать свою вещь");
        }

        // Проверяем доступность вещи
        if (!item.getAvailable()) {
            throw new ItemNotAvailableException("Вещь недоступна для бронирования");
        }

        // Валидация дат
        validateBookingDates(bookingCreateDto.getStart(), bookingCreateDto.getEnd());

        // Проверяем пересечение с другими бронированиями
        if (bookingRepository.existsOverlappingBooking(item.getId(),
                bookingCreateDto.getStart(), bookingCreateDto.getEnd())) {
            throw new ValidationException("Вещь уже забронирована на указанные даты");
        }

        // Создаем бронирование
        Booking booking = Booking.builder()
                .start(bookingCreateDto.getStart())
                .end(bookingCreateDto.getEnd())
                .item(item)
                .booker(booker)
                .status(BookingStatus.WAITING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование создано с ID: {}", savedBooking.getId());

        return bookingMapper.toDto(savedBooking);
    }

    @Override
    @Transactional
    public BookingDto approveBooking(Long bookingId, Long userId, Boolean approved) {
        log.info("Подтверждение бронирования {} пользователем {}: {}",
                bookingId, userId, approved);

        // Получаем бронирование с деталями
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Проверяем, что пользователь - владелец вещи
        if (!booking.getItem().getOwner().getId().equals(userId)) {
            throw new AccessDeniedException("Подтвердить бронирование может только владелец вещи");
        }

        // Проверяем, что бронирование еще не обработано
        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже изменен");
        }

        // Устанавливаем новый статус
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);

        Booking updatedBooking = bookingRepository.save(booking);
        log.info("Бронирование {} обновлено: статус {}",
                bookingId, updatedBooking.getStatus());

        return bookingMapper.toDto(updatedBooking);
    }

    @Override
    public BookingDto getBookingById(Long bookingId, Long userId) {
        log.info("Получение бронирования {} пользователем {}", bookingId, userId);

        // Получаем бронирование с деталями
        Booking booking = bookingRepository.findByIdWithDetails(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        // Проверяем права доступа
        Long bookerId = booking.getBooker().getId();
        Long ownerId = booking.getItem().getOwner().getId();

        if (!userId.equals(bookerId) && !userId.equals(ownerId)) {
            throw new AccessDeniedException(
                    "Просмотр бронирования доступен только автору или владельцу вещи"
            );
        }

        return bookingMapper.toDto(booking);
    }

    @Override
    public List<BookingDto> getUserBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение бронирований пользователя {} с состоянием {}", userId, state);

        // Проверяем существование пользователя
        getUser(userId);

        // Валидируем параметры пагинации
        Pageable pageable = createPageable(from, size);
        BookingState bookingState = parseState(state);

        LocalDateTime now = LocalDateTime.now();

        switch (bookingState) {
            case ALL:
                return bookingMapper.toDtoList(
                        bookingRepository.findByBookerIdOrderByStartDesc(userId, pageable)
                );
            case CURRENT:
                return bookingMapper.toDtoList(
                        bookingRepository.findCurrentByBookerId(userId, now, pageable)
                );
            case PAST:
                return bookingMapper.toDtoList(
                        bookingRepository.findPastByBookerId(userId, now, pageable)
                );
            case FUTURE:
                return bookingMapper.toDtoList(
                        bookingRepository.findFutureByBookerId(userId, now, pageable)
                );
            case WAITING:
                return bookingMapper.toDtoList(
                        bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.WAITING, pageable)
                );
            case REJECTED:
                return bookingMapper.toDtoList(
                        bookingRepository.findByBookerIdAndStatusOrderByStartDesc(
                                userId, BookingStatus.REJECTED, pageable)
                );
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    @Override
    public List<BookingDto> getOwnerBookings(Long userId, String state, Integer from, Integer size) {
        log.info("Получение бронирований владельца {} с состоянием {}", userId, state);

        // Проверяем существование пользователя
        getUser(userId);

        // Валидируем параметры пагинации
        Pageable pageable = createPageable(from, size);
        BookingState bookingState = parseState(state);

        LocalDateTime now = LocalDateTime.now();

        switch (bookingState) {
            case ALL:
                return bookingMapper.toDtoList(
                        bookingRepository.findByItemOwnerId(userId, pageable)
                );
            case CURRENT:
                return bookingMapper.toDtoList(
                        bookingRepository.findCurrentByItemOwnerId(userId, now, pageable)
                );
            case PAST:
                return bookingMapper.toDtoList(
                        bookingRepository.findPastByItemOwnerId(userId, now, pageable)
                );
            case FUTURE:
                return bookingMapper.toDtoList(
                        bookingRepository.findFutureByItemOwnerId(userId, now, pageable)
                );
            case WAITING:
                return bookingMapper.toDtoList(
                        bookingRepository.findByItemOwnerIdAndStatus(
                                userId, BookingStatus.WAITING, pageable)
                );
            case REJECTED:
                return bookingMapper.toDtoList(
                        bookingRepository.findByItemOwnerIdAndStatus(
                                userId, BookingStatus.REJECTED, pageable)
                );
            default:
                throw new ValidationException("Unknown state: " + state);
        }
    }

    @Override
    public boolean canUserCommentItem(Long userId, Long itemId) {
        return bookingRepository.existsApprovedPastBookingByUserAndItem(
                itemId, userId, LocalDateTime.now()
        );
    }

    // Вспомогательные методы

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private Item getItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    private void validateBookingDates(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new ValidationException("Даты начала и окончания обязательны");
        }

        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала не может быть позже даты окончания");
        }

        if (start.isEqual(end)) {
            throw new ValidationException("Даты начала и окончания не могут совпадать");
        }

        if (start.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала должна быть в будущем");
        }
    }

    private Pageable createPageable(Integer from, Integer size) {
        if (from == null) from = 0;
        if (size == null) size = 10;

        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }

        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }

        return PageRequest.of(from / size, size, Sort.by(Sort.Direction.DESC, "start"));
    }

    private BookingState parseState(String state) {
        try {
            return BookingState.fromString(state);
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown state: " + state);
        }
    }

    // Метод для получения последнего бронирования вещи
    public BookingShortDto getLastBookingForItem(Long itemId) {
        List<Booking> lastBookings = bookingRepository.findLastBooking(
                itemId, LocalDateTime.now()
        );

        if (!lastBookings.isEmpty()) {
            return bookingMapper.toShortDto(lastBookings.get(0));
        }
        return null;
    }

    // Метод для получения следующего бронирования вещи
    public BookingShortDto getNextBookingForItem(Long itemId) {
        List<Booking> nextBookings = bookingRepository.findNextBooking(
                itemId, LocalDateTime.now()
        );

        if (!nextBookings.isEmpty()) {
            return bookingMapper.toShortDto(nextBookings.get(0));
        }
        return null;
    }
}