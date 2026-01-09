package ru.practicum.shareit.request;

import org.mapstruct.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestShortDto;
import ru.practicum.shareit.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ItemRequestMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "requester", source = "requester")
    @Mapping(target = "items", ignore = true)
    ItemRequest toEntity(ItemRequestDto itemRequestDto, User requester);

    @Mapping(target = "items", source = "items")
    ItemRequestDto toDto(ItemRequest itemRequest, List<ItemRequestDto.ItemDto> items);

    ItemRequestShortDto toShortDto(ItemRequest itemRequest);

    List<ItemRequestDto> toDtoList(List<ItemRequest> itemRequests);
    List<ItemRequestShortDto> toShortDtoList(List<ItemRequest> itemRequests);

    default List<ItemRequestDto.ItemDto> mapItems(List<Item> items) {
        if (items == null) {
            return null;
        }

        return items.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
    }

    default ItemRequestDto.ItemDto toItemDto(Item item) {
        if (item == null) {
            return null;
        }

        return ItemRequestDto.ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    @AfterMapping
    default void setRequesterId(ItemRequest itemRequest, @MappingTarget ItemRequestDto itemRequestDto) {
        if (itemRequest.getRequester() != null) {
            // В DTO нет поля requesterId, но если нужно, можно добавить
        }
    }
}