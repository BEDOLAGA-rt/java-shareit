package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;
import java.util.List;

public interface ItemService {
    ItemDto create(Long userId, ItemDto dto);
    ItemDto update(Long userId, Long itemId, ItemDto dto);
    ItemDto getById(Long itemId);
    List<ItemDto> getByOwner(Long userId);
    List<ItemDto> search(String text);
}