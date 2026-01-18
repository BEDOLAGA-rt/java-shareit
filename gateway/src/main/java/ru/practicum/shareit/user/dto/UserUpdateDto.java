package ru.practicum.shareit.user.dto;

import lombok.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    @Size(min = 1, max = 255, message = "Имя должно содержать от 1 до 255 символов")
    private String name;

    @Email(message = "Некорректный формат email")
    @Size(max = 512, message = "Email должен содержать не более 512 символов")
    private String email;
}