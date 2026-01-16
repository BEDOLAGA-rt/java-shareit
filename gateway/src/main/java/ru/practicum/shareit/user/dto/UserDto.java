package ru.practicum.shareit.user.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 1, max = 255, message = "Имя должно содержать от 1 до 255 символов")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Size(max = 512, message = "Email должен содержать не более 512 символов")
    private String email;
}