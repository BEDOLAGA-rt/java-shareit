package ru.practicum.shareit.user.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserShortDto;
import ru.practicum.shareit.user.dto.UserUpdateDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "requests", ignore = true)
    User toEntity(UserDto userDto);

    UserDto toDto(User user);

    UserShortDto toShortDto(User user);

    List<UserDto> toDtoList(List<User> users);
    List<UserShortDto> toShortDtoList(List<User> users);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "requests", ignore = true)
    void updateUserFromDto(UserUpdateDto dto, @MappingTarget User entity);

    @AfterMapping
    default void validateUser(User user, @MappingTarget UserDto userDto) {
        // Дополнительная валидация при необходимости
    }
}