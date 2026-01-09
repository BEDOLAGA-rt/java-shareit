package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

public interface UserService {

    /**
     * Создание нового пользователя
     */
    UserDto createUser(UserCreateDto userCreateDto);

    /**
     * Обновление существующего пользователя
     */
    UserDto updateUser(Long userId, UserUpdateDto userUpdateDto);

    /**
     * Получение пользователя по ID
     */
    UserDto getUserById(Long userId);

    /**
     * Получение всех пользователей
     */
    List<UserDto> getAllUsers();

    /**
     * Удаление пользователя по ID
     */
    void deleteUser(Long userId);

    /**
     * Проверка существования пользователя
     */
    boolean existsById(Long userId);

    /**
     * Получение пользователя по email
     */
    UserDto getUserByEmail(String email);
}