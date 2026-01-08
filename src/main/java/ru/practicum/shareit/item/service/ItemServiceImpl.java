package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class ItemServiceImpl implements ItemService {

    private final Map<Long, Item> items = new HashMap<>();
    private final UserService userService;
    private long nextId = 1;

    public ItemServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ItemDto create(Long userId, ItemDto dto) {
        userService.getById(userId); // 🔴 проверка существования пользователя
        validateItem(dto);

        Item item = ItemMapper.toItem(dto, userId);
        item.setId(nextId++);
        items.put(item.getId(), item);

        return ItemMapper.toDto(item);
    }

    @Override
    public ItemDto update(Long userId, Long itemId, ItemDto dto) {
        Item item = items.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Item not found");
        }

        if (!item.getOwnerId().equals(userId)) {
            throw new NoSuchElementException("Not owner");
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
        Item item = items.get(itemId);
        if (item == null) {
            throw new NoSuchElementException("Item not found");
        }
        return ItemMapper.toDto(item);
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

        String query = text.toLowerCase();

        return items.values().stream()
                .filter(Item::getAvailable)
                .filter(item ->
                        item.getName().toLowerCase().contains(query)
                                || item.getDescription().toLowerCase().contains(query)
                )
                .map(ItemMapper::toDto)
                .toList();
    }

    private void validateItem(ItemDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Item name is empty");
        }

        if (dto.getDescription() == null || dto.getDescription().isBlank()) {
            throw new IllegalArgumentException("Item description is empty");
        }

        if (dto.getAvailable() == null) {
            throw new IllegalArgumentException("Available must be specified");
        }
    }
}