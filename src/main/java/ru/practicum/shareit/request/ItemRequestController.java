package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestCreateDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ItemRequestDto> createRequest(
            @Valid @RequestBody ItemRequestCreateDto itemRequestCreateDto,
            @RequestHeader("X-Sharer-User-Id") Long requesterId) {

        log.info("POST /requests - создание запроса пользователем {}", requesterId);
        ItemRequestDto itemRequestDto = itemRequestService.createRequest(itemRequestCreateDto, requesterId);
        return ResponseEntity.status(HttpStatus.CREATED).body(itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<List<ItemRequestDto>> getUserRequests(
            @RequestHeader("X-Sharer-User-Id") Long requesterId) {

        log.info("GET /requests - запросы пользователя {}", requesterId);
        List<ItemRequestDto> requests = itemRequestService.getUserRequests(requesterId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequests(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {

        log.info("GET /requests/all?from={}&size={} - все запросы других пользователей для userId={}",
                from, size, userId);
        List<ItemRequestDto> requests = itemRequestService.getAllRequests(userId, from, size);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ItemRequestDto> getRequestById(
            @PathVariable Long requestId,
            @RequestHeader("X-Sharer-User-Id") Long userId) {

        log.info("GET /requests/{} - получение запроса пользователем {}", requestId, userId);
        ItemRequestDto itemRequestDto = itemRequestService.getRequestById(requestId, userId);
        return ResponseEntity.ok(itemRequestDto);
    }

    // Эндпоинт для администрации (опционально)
    @GetMapping("/admin/all")
    public ResponseEntity<List<ItemRequestDto>> getAllRequestsAdmin() {
        log.info("GET /requests/admin/all - все запросы (администрация)");
        List<ItemRequestDto> requests = itemRequestService.getAllRequests();
        return ResponseEntity.ok(requests);
    }
}