package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.RequestNotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestDto createRequest(ItemRequestCreateDto itemRequestCreateDto, Long requesterId) {
        log.info("Создание запроса пользователем {}: {}", requesterId, itemRequestCreateDto);

        // Проверяем существование пользователя
        User requester = getUser(requesterId);

        // Валидируем описание
        validateDescription(itemRequestCreateDto.getDescription());

        // Создаем запрос
        ItemRequest itemRequest = ItemRequest.builder()
                .description(itemRequestCreateDto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();

        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        log.info("Запрос создан с ID: {}", savedRequest.getId());

        return itemRequestMapper.toDto(savedRequest, Collections.emptyList());
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long requesterId) {
        log.info("Получение запросов пользователя {}", requesterId);

        // Проверяем существование пользователя
        getUser(requesterId);

        // Получаем запросы пользователя с вещами
        List<ItemRequest> requests = itemRequestRepository.findByRequesterIdWithItems(requesterId);

        // Преобразуем в DTO
        return requests.stream()
                .map(request -> itemRequestMapper.toDto(request, mapItems(request.getItems())))
                .sorted(Comparator.comparing(ItemRequestDto::getCreated).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId, Long userId) {
        log.info("Получение запроса {} пользователем {}", requestId, userId);

        // Проверяем существование пользователя
        getUser(userId);

        // Получаем запрос с вещами
        ItemRequest itemRequest = itemRequestRepository.findByIdWithItems(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));

        return itemRequestMapper.toDto(itemRequest, mapItems(itemRequest.getItems()));
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId, Integer from, Integer size) {
        log.info("Получение всех запросов других пользователей для userId={} с пагинацией from={}, size={}",
                userId, from, size);

        // Проверяем существование пользователя
        getUser(userId);

        // Валидируем параметры пагинации
        Pageable pageable = createPageable(from, size, Sort.by(Sort.Direction.DESC, "created"));

        // Получаем запросы других пользователей
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterIdNot(userId, pageable);

        // Преобразуем в DTO
        return requests.stream()
                .map(request -> itemRequestMapper.toDto(request, mapItems(request.getItems())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {
        log.info("Получение всех запросов (администрация)");

        List<ItemRequest> requests = itemRequestRepository.findAllWithItems();

        return requests.stream()
                .map(request -> itemRequestMapper.toDto(request, mapItems(request.getItems())))
                .sorted(Comparator.comparing(ItemRequestDto::getCreated).reversed())
                .collect(Collectors.toList());
    }

    // Вспомогательные методы

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
    }

    private void validateDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }

        if (description.trim().length() > 2000) {
            throw new ValidationException("Описание запроса слишком длинное (максимум 2000 символов)");
        }
    }

    private Pageable createPageable(Integer from, Integer size, Sort sort) {
        if (from == null) from = 0;
        if (size == null) size = 10;

        validatePaginationParameters(from, size);

        return PageRequest.of(from / size, size, sort);
    }

    private void validatePaginationParameters(Integer from, Integer size) {
        if (from < 0) {
            throw new ValidationException("Параметр 'from' не может быть отрицательным");
        }

        if (size <= 0) {
            throw new ValidationException("Параметр 'size' должен быть положительным");
        }
    }

    private List<ItemRequestDto.ItemDto> mapItems(List<Item> items) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }

        return items.stream()
                .map(item -> ItemRequestDto.ItemDto.builder()
                        .id(item.getId())
                        .name(item.getName())
                        .description(item.getDescription())
                        .available(item.getAvailable())
                        .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                        .build())
                .collect(Collectors.toList());
    }

    // Дополнительные методы для проверок

    public boolean existsById(Long requestId) {
        return itemRequestRepository.existsById(requestId);
    }

    public boolean isRequester(Long requestId, Long userId) {
        return itemRequestRepository.existsByIdAndRequesterId(requestId, userId);
    }

    // Метод для получения запроса без проверки пользователя (для внутреннего использования)
    public ItemRequest getRequestEntityById(Long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new RequestNotFoundException(requestId));
    }
}