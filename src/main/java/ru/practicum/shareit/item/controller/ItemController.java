package ru.practicum.shareit.item.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {

    private final ItemService itemService;

    private static final String USER_HEADER = "X-Sharer-User-Id";

    @PostMapping
    public ResponseEntity<ItemDto> create(
            @RequestHeader(USER_HEADER) Long userId,
            @Valid @RequestBody ItemDto itemDto) {
        log.info("Creating item for user ID: {}, item: {}", userId, itemDto);
        ItemDto createdItem = itemService.create(userId, itemDto);
        return ResponseEntity.ok(createdItem);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> update(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long itemId,
            @RequestBody ItemDto itemDto) {
        log.info("Updating item with ID: {} for user ID: {}, update data: {}", itemId, userId, itemDto);
        ItemDto updatedItem = itemService.update(userId, itemId, itemDto);
        return ResponseEntity.ok(updatedItem);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemResponseDto> getById(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long itemId) {
        log.info("Getting item by ID: {}, for user ID: {}", itemId, userId);
        ItemResponseDto item = itemService.getById(itemId, userId);
        return ResponseEntity.ok(item);
    }

    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> getAllByOwner(
            @RequestHeader(USER_HEADER) Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting items for owner ID: {}, from: {}, size: {}", userId, from, size);
        List<ItemResponseDto> items = itemService.getAllByOwner(userId, from, size);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> search(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Searching items with text: '{}', from: {}, size: {}", text, from, size);
        List<ItemDto> items = itemService.search(text, from, size);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<CommentDto> addComment(
            @RequestHeader(USER_HEADER) Long userId,
            @PathVariable Long itemId,
            @Valid @RequestBody CommentRequestDto commentRequestDto) {
        log.info("Adding comment for item ID: {} by user ID: {}", itemId, userId);
        CommentDto comment = itemService.addComment(userId, itemId, commentRequestDto);
        return ResponseEntity.ok(comment);
    }
}