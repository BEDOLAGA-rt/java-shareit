package ru.practicum.shareit.item.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemResponseDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.Collections;

@UtilityClass
public class ItemMapper {

    public Item toItem(ItemDto dto, User owner) {
        return Item.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .available(dto.getAvailable())
                .owner(owner)
                .requestId(dto.getRequestId())
                .build();
    }

    public ItemDto toDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }

    public ItemResponseDto toResponseDto(Item item,
                                         ItemResponseDto.BookingInfo lastBooking,
                                         ItemResponseDto.BookingInfo nextBooking) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(Collections.emptyList())
                .requestId(item.getRequestId())
                .build();
    }

    public ItemResponseDto toResponseDtoWithComments(Item item,
                                                     ItemResponseDto.BookingInfo lastBooking,
                                                     ItemResponseDto.BookingInfo nextBooking,
                                                     java.util.List<ru.practicum.shareit.item.dto.CommentDto> comments) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .requestId(item.getRequestId())
                .build();
    }
}