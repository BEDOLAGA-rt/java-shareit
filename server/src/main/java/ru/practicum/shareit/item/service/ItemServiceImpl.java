package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Item item = itemMapper.toItem(itemDto);
        item.setOwner(owner);

        if (itemDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос с ID " + itemDto.getRequestId() + " не найден"));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        return itemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не является владельцем вещи");
        }

        if (itemDto.getName() != null) {
            existingItem.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null) {
            existingItem.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            existingItem.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(existingItem);
        return itemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        ItemWithBookingsDto itemDto = itemMapper.toItemWithBookingsDto(item);

        // Добавляем информацию о бронированиях для владельца
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            List<Booking> lastBookings = bookingRepository.findLastBookingForItem(itemId, now);
            List<Booking> nextBookings = bookingRepository.findNextBookingForItem(itemId, now);

            if (!lastBookings.isEmpty()) {
                Booking lastBooking = lastBookings.get(0);
                ItemWithBookingsDto.BookingShortDto shortDto = new ItemWithBookingsDto.BookingShortDto();
                shortDto.setId(lastBooking.getId());
                shortDto.setBookerId(lastBooking.getBooker().getId());
                shortDto.setStart(lastBooking.getStart());
                shortDto.setEnd(lastBooking.getEnd());
                itemDto.setLastBooking(shortDto);
            }

            if (!nextBookings.isEmpty()) {
                Booking nextBooking = nextBookings.get(0);
                ItemWithBookingsDto.BookingShortDto shortDto = new ItemWithBookingsDto.BookingShortDto();
                shortDto.setId(nextBooking.getId());
                shortDto.setBookerId(nextBooking.getBooker().getId());
                shortDto.setStart(nextBooking.getStart());
                shortDto.setEnd(nextBooking.getEnd());
                itemDto.setNextBooking(shortDto);
            }
        }

        // Добавляем комментарии
        List<Comment> comments = commentRepository.findByItemId(itemId);
        itemDto.setComments(comments.stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList()));

        return itemDto;
    }

    @Override
    public List<ItemWithBookingsDto> getUserItems(Long userId, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.findByOwnerIdOrderById(userId, pageable);

        LocalDateTime now = LocalDateTime.now();
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        // Получаем последние бронирования для всех вещей
        Map<Long, List<Booking>> lastBookingsMap = bookingRepository.findLastBookingForItems(itemIds, now)
                .stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        // Получаем следующие бронирования для всех вещей
        Map<Long, List<Booking>> nextBookingsMap = bookingRepository.findNextBookingForItems(itemIds, now)
                .stream()
                .collect(Collectors.groupingBy(booking -> booking.getItem().getId()));

        // Получаем комментарии для всех вещей
        Map<Long, List<Comment>> commentsMap = commentRepository.findByItemIdIn(itemIds)
                .stream()
                .collect(Collectors.groupingBy(comment -> comment.getItem().getId()));

        return items.stream().map(item -> {
            ItemWithBookingsDto dto = itemMapper.toItemWithBookingsDto(item);

            // Добавляем последнее бронирование
            List<Booking> lastBookings = lastBookingsMap.getOrDefault(item.getId(), Collections.emptyList());
            if (!lastBookings.isEmpty()) {
                Booking lastBooking = lastBookings.get(0);
                ItemWithBookingsDto.BookingShortDto shortDto = new ItemWithBookingsDto.BookingShortDto();
                shortDto.setId(lastBooking.getId());
                shortDto.setBookerId(lastBooking.getBooker().getId());
                shortDto.setStart(lastBooking.getStart());
                shortDto.setEnd(lastBooking.getEnd());
                dto.setLastBooking(shortDto);
            }

            // Добавляем следующее бронирование
            List<Booking> nextBookings = nextBookingsMap.getOrDefault(item.getId(), Collections.emptyList());
            if (!nextBookings.isEmpty()) {
                Booking nextBooking = nextBookings.get(0);
                ItemWithBookingsDto.BookingShortDto shortDto = new ItemWithBookingsDto.BookingShortDto();
                shortDto.setId(nextBooking.getId());
                shortDto.setBookerId(nextBooking.getBooker().getId());
                shortDto.setStart(nextBooking.getStart());
                shortDto.setEnd(nextBooking.getEnd());
                dto.setNextBooking(shortDto);
            }

            // Добавляем комментарии
            List<Comment> comments = commentsMap.getOrDefault(item.getId(), Collections.emptyList());
            dto.setComments(comments.stream()
                    .map(commentMapper::toCommentDto)
                    .collect(Collectors.toList()));

            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text, Integer from, Integer size) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }

        Pageable pageable = PageRequest.of(from / size, size);
        List<Item> items = itemRepository.searchAvailableItems(text, pageable);
        return items.stream()
                .map(itemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentDto commentDto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с ID " + itemId + " не найдена"));

        // Проверяем, что пользователь действительно брал вещь в аренду
        List<Booking> completedBookings = bookingRepository.findCompletedBookingsByUserAndItem(
                itemId, userId, LocalDateTime.now());

        if (completedBookings.isEmpty()) {
            throw new BadRequestException("Пользователь не брал эту вещь в аренду");
        }

        Comment comment = commentMapper.toComment(commentDto);
        comment.setItem(item);
        comment.setAuthor(author);
        comment.setCreated(LocalDateTime.now());

        Comment savedComment = commentRepository.save(comment);
        return commentMapper.toCommentDto(savedComment);
    }
}