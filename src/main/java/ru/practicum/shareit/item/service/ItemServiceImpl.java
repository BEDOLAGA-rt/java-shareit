package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.BadRequestException;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto create(Long userId, ItemDto itemDto) {
        User owner = getUserOrThrow(userId);
        validateItemDto(itemDto);

        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);

        log.info("Created item with ID: {} for user ID: {}", savedItem.getId(), userId);
        return ItemMapper.toDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto update(Long userId, Long itemId, ItemDto itemDto) {
        getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("User is not the owner of the item");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }

        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }

        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        log.info("Updated item with ID: {}", itemId);

        return ItemMapper.toDto(updatedItem);
    }

    @Override
    public ItemResponseDto getById(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);

        ItemResponseDto.BookingInfo lastBooking = null;
        ItemResponseDto.BookingInfo nextBooking = null;

        if (item.getOwner().getId().equals(userId)) {
            lastBooking = getLastBooking(itemId);
            nextBooking = getNextBooking(itemId);
        }

        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toDto)
                .collect(Collectors.toList());

        return ItemMapper.toResponseDtoWithComments(item, lastBooking, nextBooking, comments);
    }

    @Override
    public List<ItemResponseDto> getAllByOwner(Long userId, Integer from, Integer size) {
        getUserOrThrow(userId);

        PageRequest pageRequest = PageRequest.of(from / size, size, Sort.by(Sort.Direction.ASC, "id"));
        List<Item> items = itemRepository.findByOwnerId(userId, pageRequest);
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());

        Map<Long, ItemResponseDto.BookingInfo> lastBookings = getLastBookings(itemIds);
        Map<Long, ItemResponseDto.BookingInfo> nextBookings = getNextBookings(itemIds);
        Map<Long, List<CommentDto>> commentsByItem = getCommentsByItem(itemIds);

        return items.stream()
                .map(item -> {
                    ItemResponseDto.BookingInfo lastBooking = lastBookings.get(item.getId());
                    ItemResponseDto.BookingInfo nextBooking = nextBookings.get(item.getId());
                    List<CommentDto> comments = commentsByItem.getOrDefault(item.getId(), Collections.emptyList());

                    return ItemResponseDto.builder()
                            .id(item.getId())
                            .name(item.getName())
                            .description(item.getDescription())
                            .available(item.getAvailable())
                            .lastBooking(lastBooking)
                            .nextBooking(nextBooking)
                            .comments(comments)
                            .requestId(item.getRequestId())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text, Integer from, Integer size) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        PageRequest pageRequest = PageRequest.of(from / size, size);
        String searchText = text.toLowerCase();

        return itemRepository.searchAvailableItems(searchText, pageRequest).stream()
                .map(ItemMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        validateCommentRequest(commentRequestDto);
        validateUserBookedItem(userId, itemId);

        Comment comment = Comment.builder()
                .text(commentRequestDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
        log.info("Added comment with ID: {} for item ID: {} by user ID: {}",
                savedComment.getId(), itemId, userId);

        return CommentMapper.toDto(savedComment);
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item not found with ID: " + itemId));
    }

    private void validateItemDto(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new BadRequestException("Item name cannot be empty");
        }

        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new BadRequestException("Item description cannot be empty");
        }

        if (itemDto.getAvailable() == null) {
            throw new BadRequestException("Item availability must be specified");
        }
    }

    private void validateCommentRequest(CommentRequestDto commentRequestDto) {
        if (commentRequestDto.getText() == null || commentRequestDto.getText().isBlank()) {
            throw new BadRequestException("Comment text cannot be empty");
        }
    }

    private void validateUserBookedItem(Long userId, Long itemId) {
        List<Booking> bookings = bookingRepository.findByItemIdAndBookerIdAndStatusAndEndBefore(
                itemId, userId, ru.practicum.shareit.booking.BookingStatus.APPROVED, LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new BadRequestException("User can only comment on items they have booked in the past");
        }
    }

    private ItemResponseDto.BookingInfo getLastBooking(Long itemId) {
        return bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(itemId, LocalDateTime.now())
                .map(booking -> ItemResponseDto.BookingInfo.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build())
                .orElse(null);
    }

    private ItemResponseDto.BookingInfo getNextBooking(Long itemId) {
        return bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, LocalDateTime.now())
                .map(booking -> ItemResponseDto.BookingInfo.builder()
                        .id(booking.getId())
                        .bookerId(booking.getBooker().getId())
                        .build())
                .orElse(null);
    }

    private Map<Long, ItemResponseDto.BookingInfo> getLastBookings(List<Long> itemIds) {
        return bookingRepository.findLastBookingsForItems(itemIds, LocalDateTime.now()).stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> ItemResponseDto.BookingInfo.builder()
                                .id(booking.getId())
                                .bookerId(booking.getBooker().getId())
                                .build(),
                        (existing, replacement) -> existing
                ));
    }

    private Map<Long, ItemResponseDto.BookingInfo> getNextBookings(List<Long> itemIds) {
        return bookingRepository.findNextBookingsForItems(itemIds, LocalDateTime.now()).stream()
                .collect(Collectors.toMap(
                        booking -> booking.getItem().getId(),
                        booking -> ItemResponseDto.BookingInfo.builder()
                                .id(booking.getId())
                                .bookerId(booking.getBooker().getId())
                                .build(),
                        (existing, replacement) -> existing
                ));
    }

    private Map<Long, List<CommentDto>> getCommentsByItem(List<Long> itemIds) {
        return commentRepository.findByItemIdIn(itemIds).stream()
                .collect(Collectors.groupingBy(
                        comment -> comment.getItem().getId(),
                        Collectors.mapping(CommentMapper::toDto, Collectors.toList())
                ));
    }
}