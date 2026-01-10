package ru.practicum.shareit.booking.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BookingItemDto {
    private Long id;
    private String name;
}