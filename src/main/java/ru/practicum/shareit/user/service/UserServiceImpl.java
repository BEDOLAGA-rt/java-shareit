package ru.practicum.shareit.user.service;


import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.User;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    private final Map<Long, User> users = new HashMap<>();
    private long nextId = 1;

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        user.setId(nextId++);
        users.put(user.getId(), user);
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto update(Long id, UserDto userDto) {
        User user = users.get(id);
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toDto(user);
    }

    @Override
    public UserDto getById(Long id) {
        return UserMapper.toDto(users.get(id));
    }

    @Override
    public List<UserDto> getAll() {
        return users.values().stream()
                .map(UserMapper::toDto)
                .toList();
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
    }
}