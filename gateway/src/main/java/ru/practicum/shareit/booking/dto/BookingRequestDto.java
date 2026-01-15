package ru.practicum.shareit.booking.dto;

import lombok.*;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequestDto {
	@NotNull(message = "ID вещи не может быть пустым")
	private Long itemId;

	@FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом")
	@NotNull(message = "Дата начала не может быть пустой")
	private LocalDateTime start;

	@Future(message = "Дата окончания бронирования должна быть в будущем")
	@NotNull(message = "Дата окончания не может быть пустой")
	private LocalDateTime end;
}