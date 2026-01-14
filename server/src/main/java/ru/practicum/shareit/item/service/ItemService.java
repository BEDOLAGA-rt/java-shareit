package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;

import java.util.List;

public interface ItemService {
    ItemDto createItem(Long userId, ItemDto itemDto);

    ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto);

    ItemWithBookingsDto getItemById(Long userId, Long itemId);

    List<ItemWithBookingsDto> getUserItems(Long userId, Integer from, Integer size);

    List<ItemDto> searchItems(String text, Integer from, Integer size);

    CommentDto addComment(Long userId, Long itemId, CommentDto commentDto);
}