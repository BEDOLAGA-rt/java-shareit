package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        // Проверяем уникальность email перед сохранением
        userRepository.findByEmail(userDto.getEmail())
                .ifPresent(u -> {
                    throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
                });

        User user = userMapper.toUser(userDto);

        // Оставляем только один try-catch на случай конкурентного доступа
        try {
            User savedUser = userRepository.save(user);
            return userMapper.toUserDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            // Проверяем, что новый email не занят другим пользователем
            userRepository.findByEmail(userDto.getEmail())
                    .ifPresent(user -> {
                        if (!user.getId().equals(userId)) {
                            throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
                        }
                    });
            existingUser.setEmail(userDto.getEmail());
        }

        try {
            User updatedUser = userRepository.save(existingUser);
            return userMapper.toUserDto(updatedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Пользователь с таким email уже существует");
        }
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        return userMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}