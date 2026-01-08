package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public UserDto create(UserDto userDto) {
        validateEmail(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        user.setId(nextId++);
        users.put(user.getId(), user);

        return UserMapper.toDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User user = users.get(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }

        if (userDto.getEmail() != null) {
            validateEmail(userDto.getEmail(), id);
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        return UserMapper.toDto(user);
    }

    @Override
    public UserDto getById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NoSuchElementException("User not found");
        }
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        return users.values().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new NoSuchElementException("User not found");
        }
        users.remove(id);
    }

    private void validateEmail(String email, Long userId) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email");
        }

        for (User existing : users.values()) {
            if (existing.getEmail().equals(email)
                    && (userId == null || !existing.getId().equals(userId))) {
                throw new IllegalStateException("Email already exists");
            }
        }
    }
}