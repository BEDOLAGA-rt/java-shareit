package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.*;

import java.util.List;

public interface ItemService {

    /**
     * Создание новой вещи
     */
    ItemDto createItem(ItemCreateDto itemCreateDto, Long ownerId);

    /**
     * Обновление существующей вещи
     */
    ItemDto updateItem(Long itemId, ItemUpdateDto itemUpdateDto, Long ownerId);

    /**
     * Получение вещи по ID
     */
    ItemDto getItemById(Long itemId, Long userId);

    /**
     * Получение всех вещей владельца
     */
    List<ItemDto> getOwnerItems(Long ownerId, Integer from, Integer size);

    /**
     * Поиск доступных вещей по тексту
     */
    List<ItemDto> searchAvailableItems(String text, Integer from, Integer size);

    /**
     * Добавление комментария к вещи
     */
    CommentDto addComment(Long itemId, CommentCreateDto commentCreateDto, Long userId);

    /**
     * Получение вещей по запросу
     */
    List<ItemDto> getItemsByRequestId(Long requestId);
}