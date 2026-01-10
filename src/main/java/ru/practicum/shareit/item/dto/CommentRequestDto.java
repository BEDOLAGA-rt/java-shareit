package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDto {

    @NotBlank(message = "Text cannot be blank")
    private String text;
}