package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId, Pageable pageable);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndEndBeforeOrderByStartDesc(
            Long bookerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByBookerIdAndStartAfterOrderByStartDesc(
            Long bookerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(
            Long bookerId, BookingStatus status, Pageable pageable);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndEndBeforeOrderByStartDesc(
            Long ownerId, LocalDateTime end, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStartAfterOrderByStartDesc(
            Long ownerId, LocalDateTime start, Pageable pageable);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(
            Long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start <= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findLastBookingForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBookingForItem(@Param("itemId") Long itemId, @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :userId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    List<Booking> findCompletedBookingsByUserAndItem(@Param("itemId") Long itemId,
                                                     @Param("userId") Long userId,
                                                     @Param("now") LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndBookerIdAndEndBeforeAndStatus(
            Long itemId, Long bookerId, LocalDateTime end, BookingStatus status);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.status = 'APPROVED' " +
            "AND b.start <= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findLastBookingForItems(@Param("itemIds") List<Long> itemIds,
                                          @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.status = 'APPROVED' " +
            "AND b.start > :now " +
            "ORDER BY b.start ASC")
    List<Booking> findNextBookingForItems(@Param("itemIds") List<Long> itemIds,
                                          @Param("now") LocalDateTime now);
}