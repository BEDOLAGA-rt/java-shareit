package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        validateUserDto(userDto);

        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ConflictException("Email already exists: " + userDto.getEmail());
        }

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);

        log.info("Created user with ID: {}", savedUser.getId());
        return UserMapper.toDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        User user = getUserOrThrow(id);

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            validateEmail(userDto.getEmail(), id);
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated user with ID: {}", id);

        return UserMapper.toDto(updatedUser);
    }

    @Override
    public UserDto getById(Long id) {
        User user = getUserOrThrow(id);
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("Deleted user with ID: {}", id);
    }

    @Override
    public void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found with ID: " + userId);
        }
    }

    private User getUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + id));
    }

    private void validateUserDto(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }

        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new IllegalArgumentException("User email cannot be empty");
        }

        if (!isValidEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Invalid email format: " + userDto.getEmail());
        }
    }

    private void validateEmail(String email, Long userId) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new ConflictException("Email already exists: " + email);
        }
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}