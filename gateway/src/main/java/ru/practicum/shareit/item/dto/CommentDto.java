package ru.practicum.shareit.item.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentDto {
    private Long id;

    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(min = 1, max = 1000, message = "Текст комментария должен содержать от 1 до 1000 символов")
    private String text;

    private String authorName;
    private LocalDateTime created;
}