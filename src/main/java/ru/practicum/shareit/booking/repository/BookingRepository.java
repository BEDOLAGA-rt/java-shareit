package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByItemIdInOrderByStartDesc(List<Long> itemIds);

    boolean existsByBookerIdAndItemIdAndEndBefore(Long bookerId,
                                                  Long itemId,
                                                  LocalDateTime time);
}