package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Основные методы
    List<Booking> findByItemIdOrderByStartDesc(Long itemId);

    List<Booking> findByItemIdInOrderByStartDesc(List<Long> itemIds);

    Page<Booking> findByBookerId(Long bookerId, Pageable pageable);

    Page<Booking> findByBookerIdAndStatus(Long bookerId, BookingStatus status, Pageable pageable);

    // Для получения текущих, прошлых и будущих бронирований пользователя
    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start < :now AND b.end > :now")
    Page<Booking> findCurrentBookingsByBooker(@Param("bookerId") Long bookerId,
                                              @Param("now") LocalDateTime now,
                                              Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.end < :now")
    Page<Booking> findPastBookingsByBooker(@Param("bookerId") Long bookerId,
                                           @Param("now") LocalDateTime now,
                                           Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start > :now")
    Page<Booking> findFutureBookingsByBooker(@Param("bookerId") Long bookerId,
                                             @Param("now") LocalDateTime now,
                                             Pageable pageable);

    // Для получения бронирований владельца
    Page<Booking> findByItemOwnerId(Long ownerId, Pageable pageable);

    Page<Booking> findByItemOwnerIdAndStatus(Long ownerId, BookingStatus status, Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start < :now AND b.end > :now")
    Page<Booking> findCurrentBookingsByOwner(@Param("ownerId") Long ownerId,
                                             @Param("now") LocalDateTime now,
                                             Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.end < :now")
    Page<Booking> findPastBookingsByOwner(@Param("ownerId") Long ownerId,
                                          @Param("now") LocalDateTime now,
                                          Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start > :now")
    Page<Booking> findFutureBookingsByOwner(@Param("ownerId") Long ownerId,
                                            @Param("now") LocalDateTime now,
                                            Pageable pageable);

    // Для проверки возможности оставить комментарий
    List<Booking> findByItemIdAndBookerIdAndStatusAndEndBefore(
            Long itemId, Long bookerId, BookingStatus status, LocalDateTime end);

    // Для получения последнего бронирования (для ItemServiceImpl)
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.end < :now " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.end DESC")
    Optional<Booking> findFirstByItemIdAndEndBeforeOrderByEndDesc(
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now);

    // Для получения следующего бронирования (для ItemServiceImpl)
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.start > :now " +
            "AND b.status = 'APPROVED' " +
            "ORDER BY b.start ASC")
    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(
            @Param("itemId") Long itemId,
            @Param("now") LocalDateTime now);

    // Для получения последних бронирований для списка вещей (для ItemServiceImpl)
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.end < :now " +
            "AND b.status = 'APPROVED' " +
            "AND b.id IN (" +
            "   SELECT MAX(b2.id) FROM Booking b2 " +
            "   WHERE b2.item.id IN :itemIds " +
            "   AND b2.end < :now " +
            "   AND b2.status = 'APPROVED' " +
            "   GROUP BY b2.item.id" +
            ")")
    List<Booking> findLastBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now);

    // Для получения следующих бронирований для списка вещей (для ItemServiceImpl)
    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.id IN :itemIds " +
            "AND b.start > :now " +
            "AND b.status = 'APPROVED' " +
            "AND b.id IN (" +
            "   SELECT MIN(b2.id) FROM Booking b2 " +
            "   WHERE b2.item.id IN :itemIds " +
            "   AND b2.start > :now " +
            "   AND b2.status = 'APPROVED' " +
            "   GROUP BY b2.item.id" +
            ")")
    List<Booking> findNextBookingsForItems(@Param("itemIds") List<Long> itemIds,
                                           @Param("now") LocalDateTime now);

    // Дополнительные полезные методы
    List<Booking> findByItemIdAndStatus(Long itemId, BookingStatus status);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.booker.id = :userId " +
            "AND b.status = 'APPROVED' " +
            "AND b.end < :now")
    boolean existsByItemIdAndBookerIdAndEndBefore(@Param("itemId") Long itemId,
                                                  @Param("userId") Long userId,
                                                  @Param("now") LocalDateTime now);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.item.owner.id = :ownerId " +
            "AND b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND b.start <= :now " +
            "AND b.end >= :now")
    Optional<Booking> findCurrentBookingForItemByOwner(@Param("ownerId") Long ownerId,
                                                       @Param("itemId") Long itemId,
                                                       @Param("now") LocalDateTime now);

    // Проверка пересечения дат для бронирования
    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.item.id = :itemId " +
            "AND b.status = 'APPROVED' " +
            "AND ((b.start <= :start AND b.end >= :start) " +
            "OR (b.start <= :end AND b.end >= :end) " +
            "OR (b.start >= :start AND b.end <= :end))")
    boolean existsOverlappingBooking(@Param("itemId") Long itemId,
                                     @Param("start") LocalDateTime start,
                                     @Param("end") LocalDateTime end);
}