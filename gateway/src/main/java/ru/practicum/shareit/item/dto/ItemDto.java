package ru.practicum.shareit.item.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemDto {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    @Size(min = 1, max = 255, message = "Название должно содержать от 1 до 255 символов")
    private String name;

    @NotBlank(message = "Описание не может быть пустым")
    @Size(min = 1, max = 1000, message = "Описание должно содержать от 1 до 1000 символов")
    private String description;

    @NotNull(message = "Статус доступности не может быть null")
    private Boolean available;

    private Long requestId;
}