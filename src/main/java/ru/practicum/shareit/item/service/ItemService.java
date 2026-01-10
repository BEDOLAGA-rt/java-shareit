package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.dto.CommentRequestDto;

import java.util.List;

public interface ItemService {

    ItemDto create(Long userId, ItemDto itemDto);

    ItemDto update(Long userId, Long itemId, ItemDto itemDto);

    ItemResponseDto getById(Long itemId, Long userId);

    List<ItemResponseDto> getAllByOwner(Long userId, Integer from, Integer size);

    List<ItemDto> search(String text, Integer from, Integer size);

    CommentDto addComment(Long userId, Long itemId, CommentRequestDto commentRequestDto);
}