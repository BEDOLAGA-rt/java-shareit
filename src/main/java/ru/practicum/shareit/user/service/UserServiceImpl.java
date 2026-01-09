package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserCreateDto;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;
import ru.practicum.shareit.user.mapper.UserMapper;

import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    // Паттерн для валидации email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$"
    );

    @Override
    @Transactional
    public UserDto createUser(UserCreateDto userCreateDto) {
        log.info("Создание пользователя: {}", userCreateDto);

        // Валидация данных
        validateUserCreateDto(userCreateDto);

        // Проверка уникальности email
        if (userRepository.existsByEmailIgnoreCase(userCreateDto.getEmail())) {
            throw new DuplicateEmailException(userCreateDto.getEmail());
        }

        // Создаем пользователя напрямую, без использования маппера для CreateDto
        User user = User.builder()
                .name(userCreateDto.getName())
                .email(userCreateDto.getEmail())
                .build();

        try {
            User savedUser = userRepository.save(user);
            log.info("Пользователь создан с ID: {}", savedUser.getId());
            return userMapper.toDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateEmailException(userCreateDto.getEmail());
        }
    }
    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserUpdateDto userUpdateDto) {
        log.info("Обновление пользователя {}: {}", userId, userUpdateDto);

        // Проверяем существование пользователя
        User user = getUser(userId);

        // Если обновляется email, проверяем уникальность
        if (userUpdateDto.getEmail() != null && !userUpdateDto.getEmail().equals(user.getEmail())) {
            validateEmail(userUpdateDto.getEmail());

            if (userRepository.existsByEmailAndIdNot(userUpdateDto.getEmail(), userId)) {
                throw new ConflictException(
                        String.format("Пользователь с email %s уже существует", userUpdateDto.getEmail())
                );
            }
        }

        // Обновляем поля
        userMapper.updateUserFromDto(userUpdateDto, user);

        try {
            User updatedUser = userRepository.save(user);
            log.info("Пользователь {} обновлен", userId);
            return userMapper.toDto(updatedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Нарушение уникальности email: " + userUpdateDto.getEmail());
        }
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя по ID: {}", userId);

        User user = getUser(userId);
        return userMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");

        List<User> users = userRepository.findAll();
        return userMapper.toDtoList(users);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя: {}", userId);

        // Проверяем существование пользователя
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }

        userRepository.deleteById(userId);
        log.info("Пользователь {} удален", userId);
    }

    @Override
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    @Override
    public UserDto getUserByEmail(String email) {
        log.info("Получение пользователя по email: {}", email);

        validateEmail(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с email %s не найден", email)
                ));

        return userMapper.toDto(user);
    }

    // Вспомогательные методы

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private void validateUserCreateDto(UserCreateDto userCreateDto) {
        if (userCreateDto.getName() == null || userCreateDto.getName().trim().isEmpty()) {
            throw new ValidationException("Имя пользователя не может быть пустым");
        }

        validateEmail(userCreateDto.getEmail());
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Некорректный формат email: " + email);
        }

        if (email.length() > 512) {
            throw new ValidationException("Email слишком длинный (максимум 512 символов)");
        }
    }

    // Дополнительные методы

    /**
     * Получение пользователя с его вещами
     */
    public UserDto getUserWithItems(Long userId) {
        User user = userRepository.findByIdWithItems(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toDto(user);
    }

    /**
     * Получение пользователя с его бронированиями
     */
    public UserDto getUserWithBookings(Long userId) {
        User user = userRepository.findByIdWithBookings(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toDto(user);
    }

    /**
     * Поиск пользователей по имени
     */
    public List<UserDto> searchUsersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllUsers();
        }

        List<User> users = userRepository.findByNameContainingIgnoreCase(name.trim());
        return userMapper.toDtoList(users);
    }

    /**
     * Получение пользователей с активными бронированиями
     */
    public List<UserDto> getUsersWithActiveBookings() {
        List<User> users = userRepository.findUsersWithActiveBookings();
        return userMapper.toDtoList(users);
    }

    /**
     * Проверка, может ли пользователь бронировать вещи
     */
    public boolean canUserBookItems(Long userId) {
        // Здесь можно добавить бизнес-логику, например:
        // - проверка, не заблокирован ли пользователь
        // - проверка рейтинга пользователя
        // - проверка наличия просроченных бронирований
        return existsById(userId);
    }

    /**
     * Блокировка пользователя (дополнительная функциональность)
     */
    @Transactional
    public UserDto blockUser(Long userId) {
        User user = getUser(userId);
        // Здесь можно добавить логику блокировки
        // Например, установить флаг isBlocked в true
        log.warn("Пользователь {} заблокирован", userId);
        return userMapper.toDto(userRepository.save(user));
    }
}