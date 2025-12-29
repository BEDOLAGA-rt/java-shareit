package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Service
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> items = new HashMap<>();
    private long nextId = 1;

    @Override
    public ItemDto create(Long userId, ItemDto dto) {
        Item item = ItemMapper.toItem(dto, userId);
        item.setId(nextId++);
        items.put(item.getId(), item);
        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto dto) {
        Item item = items.get(itemId);
        if (!item.getOwnerId().equals(userId)) {
            throw new RuntimeException("Редактировать может только владелец");
        }

        if (dto.getName() != null) {
            item.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            item.setDescription(dto.getDescription());
        }
        if (dto.getAvailable() != null) {
            item.setAvailable(dto.getAvailable());
        }

        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto getById(Long itemId) {
        return ItemMapper.toDto(items.get(itemId));
    }

    @Override
    public List<ItemDto> getByOwner(Long userId) {
        return items.values().stream()
                .filter(item -> item.getOwnerId().equals(userId))
                .map(ItemMapper::toDto)
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String lower = text.toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        item.getName().toLowerCase().contains(lower) ||
                                item.getDescription().toLowerCase().contains(lower))
                .map(ItemMapper::toDto)
                .toList();
    }
}