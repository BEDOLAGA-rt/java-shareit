package ru.practicum.shareit.item.mapper;

import org.mapstruct.*;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;

@Mapper(componentModel = "spring", uses = CommentMapper.class)
public interface ItemMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "request", ignore = true)
    @Mapping(target = "comments", ignore = true)
    Item toEntity(ItemCreateDto itemCreateDto, User owner);

    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "request", source = "request")
    @Mapping(target = "comments", ignore = true)
    Item toEntity(ItemDto itemDto, User owner, ItemRequest request);

    // Метод 1: с параметрами
    @Mapping(target = "id", source = "item.id")
    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "description", source = "item.description")
    @Mapping(target = "available", source = "item.available")
    @Mapping(target = "requestId", source = "item.request.id")
    @Mapping(target = "lastBooking", source = "lastBooking")
    @Mapping(target = "nextBooking", source = "nextBooking")
    @Mapping(target = "comments", source = "comments")
    ItemDto toDto(Item item,
                  @Context BookingShortDto lastBooking,
                  @Context BookingShortDto nextBooking,
                  @Context List<CommentDto> comments);

    // Метод 2: простой (переименуем, чтобы избежать конфликта)
    @Mapping(target = "requestId", source = "request.id")
    @Mapping(target = "lastBooking", ignore = true)
    @Mapping(target = "nextBooking", ignore = true)
    @Mapping(target = "comments", ignore = true)
    ItemDto toSimpleDto(Item item);

    List<ItemDto> toDtoList(List<Item> items);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "request", ignore = true)
    @Mapping(target = "comments", ignore = true)
    void updateItemFromDto(ItemUpdateDto dto, @MappingTarget Item entity);

    // Упрощенный AfterMapping
    default void setRequestIdIfNeeded(Item item, @MappingTarget ItemDto itemDto) {
        if (item.getRequest() != null && itemDto.getRequestId() == null) {
            itemDto.setRequestId(item.getRequest().getId());
        }
    }
}