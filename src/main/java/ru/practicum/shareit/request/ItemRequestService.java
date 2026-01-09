package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    /**
     * Создание нового запроса вещи
     */
    ItemRequestDto createRequest(ItemRequestCreateDto itemRequestCreateDto, Long requesterId);

    /**
     * Получение всех запросов текущего пользователя
     */
    List<ItemRequestDto> getUserRequests(Long requesterId);

    /**
     * Получение запроса по ID
     */
    ItemRequestDto getRequestById(Long requestId, Long userId);

    /**
     * Получение всех запросов других пользователей
     */
    List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size);

    /**
     * Получение всех запросов (для администрации)
     */
    List<ItemRequestDto> getAllRequests();
}