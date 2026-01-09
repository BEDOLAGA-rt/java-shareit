package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ItemDto> createItem(
            @Valid @RequestBody ItemCreateDto itemCreateDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {

        log.info("POST /items - создание вещи владельцем {}", ownerId);
        ItemDto itemDto = itemService.createItem(itemCreateDto, ownerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<ItemDto> updateItem(
            @PathVariable Long itemId,
            @Valid @RequestBody ItemUpdateDto itemUpdateDto,
            @RequestHeader("X-Sharer-User-Id") Long ownerId) {

        log.info("PATCH /items/{} - обновление вещи владельцем {}", itemId, ownerId);
        ItemDto itemDto = itemService.updateItem(itemId, itemUpdateDto, ownerId);
        return ResponseEntity.ok(itemDto);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDto> getItemById(
            @PathVariable Long itemId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("GET /items/{} - получение вещи пользователем {}", itemId, userId);
        ItemDto itemDto = itemService.getItemById(itemId, userId);
        return ResponseEntity.ok(itemDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> getOwnerItems(
            @RequestHeader("X-Sharer-User-Id") Long ownerId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /items?from={}&size={} - вещи владельца {}", from, size, ownerId);
        List<ItemDto> items = itemService.getOwnerItems(ownerId, from, size);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ItemDto>> searchAvailableItems(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /items/search?text={}&from={}&size={} - поиск вещей",
                text, from, size);
        List<ItemDto> items = itemService.searchAvailableItems(text, from, size);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CommentDto> addComment(
            @PathVariable Long itemId,
            @Valid @RequestBody CommentCreateDto commentCreateDto,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("POST /items/{}/comment - добавление комментария пользователем {}",
                itemId, userId);
        CommentDto commentDto = itemService.addComment(itemId, commentCreateDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(commentDto);
    }
}