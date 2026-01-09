package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final BookingService bookingService;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public ItemDto createItem(ItemCreateDto itemCreateDto, Long ownerId) {
        log.info("Создание вещи владельцем {}: {}", ownerId, itemCreateDto);

        // Проверяем существование пользователя
        User owner = getUser(ownerId);

        // Создаем вещь
        Item item = itemMapper.toEntity(itemCreateDto, owner);

        // Устанавливаем запрос, если указан
        if (itemCreateDto.getRequestId() != null) {
            ItemRequest request = itemRequestRepository.findById(itemCreateDto.getRequestId())
                    .orElseThrow(() -> new RequestNotFoundException(itemCreateDto.getRequestId()));
            item.setRequest(request);
        }

        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана с ID: {}", savedItem.getId());

        return itemMapper.toSimpleDto(savedItem); // ИЗМЕНЕНО: toDto -> toSimpleDto
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long itemId, ItemUpdateDto itemUpdateDto, Long ownerId) {
        log.info("Обновление вещи {} владельцем {}: {}", itemId, ownerId, itemUpdateDto);

        // Проверяем существование вещи
        Item item = getItem(itemId);

        // Проверяем права доступа
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new AccessDeniedException(
                    String.format("Пользователь %d не является владельцем вещи %d", ownerId, itemId)
            );
        }

        // Обновляем поля
        itemMapper.updateItemFromDto(itemUpdateDto, item);

        // Сохраняем изменения
        Item updatedItem = itemRepository.save(item);
        log.info("Вещь {} обновлена", itemId);

        return itemMapper.toSimpleDto(updatedItem); // ИЗМЕНЕНО: toDto -> toSimpleDto
    }

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        log.info("Получение вещи {} пользователем {}", itemId, userId);

        // Получаем вещь
        Item item = getItem(itemId);

        // Получаем комментарии
        List<CommentDto> comments = getCommentsForItem(itemId);

        // Если пользователь - владелец, получаем информацию о бронированиях
        if (item.getOwner().getId().equals(userId)) {
            BookingShortDto lastBooking = bookingService.getLastBookingForItem(itemId);
            BookingShortDto nextBooking = bookingService.getNextBookingForItem(itemId);

            return itemMapper.toDto(item, lastBooking, nextBooking, comments);
        }

        // Для других пользователей информация о бронированиях не отображается
        return itemMapper.toDto(item, null, null, comments);
    }

    @Override
    public List<ItemDto> getOwnerItems(Long ownerId, Integer from, Integer size) {
        log.info("Получение вещей владельца {} с пагинацией from={}, size={}", ownerId, from, size);

        // Проверяем существование пользователя
        getUser(ownerId);

        // Создаем пагинацию
        Pageable pageable = createPageable(from, size, Sort.by("id").ascending());

        // Получаем вещи владельца
        List<Item> items = itemRepository.findByOwnerId(ownerId, pageable);

        // Собираем ID вещей
        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        // Получаем комментарии для всех вещей
        Map<Long, List<CommentDto>> commentsByItem = getCommentsForItems(itemIds);

        // Получаем информацию о бронированиях
        Map<Long, BookingShortDto> lastBookings = new HashMap<>();
        Map<Long, BookingShortDto> nextBookings = new HashMap<>();

        for (Item item : items) {
            lastBookings.put(item.getId(), bookingService.getLastBookingForItem(item.getId()));
            nextBookings.put(item.getId(), bookingService.getNextBookingForItem(item.getId()));
        }

        // Преобразуем в DTO
        return items.stream()
                .map(item -> itemMapper.toDto(
                        item,
                        lastBookings.get(item.getId()),
                        nextBookings.get(item.getId()),
                        commentsByItem.getOrDefault(item.getId(), Collections.emptyList())
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchAvailableItems(String text, Integer from, Integer size) {
        log.info("Поиск доступных вещей по тексту '{}' с пагинацией from={}, size={}", text, from, size);

        // Если текст пустой, возвращаем пустой список
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        // Создаем пагинацию
        Pageable pageable = createPageable(from, size);

        // Ищем вещи
        List<Item> items = itemRepository.searchAvailableItems(text.trim(), pageable);

        // Преобразуем в DTO - ИЗМЕНЕНО: используем toSimpleDto вместо toDto
        return items.stream()
                .map(itemMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long itemId, CommentCreateDto commentCreateDto, Long userId) {
        log.info("Добавление комментария к вещи {} пользователем {}", itemId, userId);

        // Проверяем существование пользователя
        User author = getUser(userId);

        // Проверяем существование вещи
        Item item = getItem(itemId);

        // Проверяем, что пользователь брал вещь в аренду и аренда завершена
        if (!bookingService.canUserCommentItem(userId, itemId)) {
            throw new ValidationException(
                    "Пользователь может оставлять комментарии только к вещам, которые он брал в аренду"
            );
        }

        // Создаем комментарий
        Comment comment = commentMapper.toEntity(commentCreateDto, item, author);

        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий добавлен с ID: {}", savedComment.getId());

        return commentMapper.toDto(savedComment);
    }

    @Override
    public List<ItemDto> getItemsByRequestId(Long requestId) {
        log.info("Получение вещей по запросу {}", requestId);

        List<Item> items = itemRepository.findByRequestId(requestId);

        // ИЗМЕНЕНО: используем toSimpleDto вместо toDto
        return items.stream()
                .map(itemMapper::toSimpleDto)
                .collect(Collectors.toList());
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

    private Pageable createPageable(Integer from, Integer size) {
        return createPageable(from, size, Sort.unsorted());
    }

    private Pageable createPageable(Integer from, Integer size, Sort sort) {
        if (from == null) from = 0;
        if (size == null) size = 10;

        validatePaginationParameters(from, size);

        return PageRequest.of(from / size, size, sort);
    }

    private void validatePaginationParameters(Integer from, Integer size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }

        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }
    }

    private List<CommentDto> getCommentsForItem(Long itemId) {
        List<Comment> comments = commentRepository.findByItemIdWithAuthor(itemId);
        return commentMapper.toDtoList(comments);
    }

    private Map<Long, List<CommentDto>> getCommentsForItems(List<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Comment> comments = commentRepository.findByItemIds(itemIds);

        return comments.stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(commentMapper::toDto, Collectors.toList())
                ));
    }

    // Метод для получения всех вещей (для администрирования)
    public List<ItemDto> getAllItems(Integer from, Integer size) {
        Pageable pageable = createPageable(from, size, Sort.by("id").ascending());
        List<Item> items = itemRepository.findAll(pageable).getContent();

        // ИЗМЕНЕНО: используем toSimpleDtoList или toDtoList в зависимости от того, что есть в маппере
        // Если в ItemMapper есть toDtoList, используем его, иначе собираем через stream
        return items.stream()
                .map(itemMapper::toSimpleDto)
                .collect(Collectors.toList());
    }

    // Метод для проверки существования вещи
    public boolean existsById(Long itemId) {
        return itemRepository.existsById(itemId);
    }

    // Метод для проверки, является ли пользователь владельцем вещи
    public boolean isOwner(Long itemId, Long userId) {
        return itemRepository.existsByIdAndOwnerId(itemId, userId);
    }
}