package ru.practicum.shareit.booking;

import org.mapstruct.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "item", source = "item")
    @Mapping(target = "booker", source = "booker")
    @Mapping(target = "status", source = "status")
    Booking toEntity(BookingDto bookingDto, Item item, User booker, BookingStatus status);

    // Простой маппинг без деталей
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "bookerId", source = "booker.id")
    @Mapping(target = "item", ignore = true)
    @Mapping(target = "booker", ignore = true)
    BookingDto toDto(Booking booking);

    // Маппинг с деталями
    @Mapping(target = "itemId", source = "item.id")
    @Mapping(target = "bookerId", source = "booker.id")
    @Mapping(target = "item.id", source = "item.id")
    @Mapping(target = "item.name", source = "item.name")
    @Mapping(target = "booker.id", source = "booker.id")
    @Mapping(target = "booker.name", source = "booker.name")
    BookingDto toDtoWithDetails(Booking booking);

    // Маппинг в BookingShortDto
    @Mapping(target = "bookerId", source = "booker.id")
    @Mapping(target = "itemId", source = "item.id")
    BookingShortDto toShortDto(Booking booking);

    List<BookingDto> toDtoList(List<Booking> bookings);

    List<BookingShortDto> toShortDtoList(List<Booking> bookings);

    // Альтернативно, если хотите использовать default методы:
    default BookingShortDto toBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingShortDto.builder()
                .id(booking.getId())
                .bookerId(booking.getBooker() != null ? booking.getBooker().getId() : null)
                .itemId(booking.getItem() != null ? booking.getItem().getId() : null)
                .start(booking.getStart())
                .end(booking.getEnd())
                .status(booking.getStatus())
                .build();
    }
}