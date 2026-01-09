package ru.practicum.shareit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Поиск пользователя по email (игнорируя регистр)
    Optional<User> findByEmailIgnoreCase(String email);

    // Поиск пользователя по email (точное совпадение)
    Optional<User> findByEmail(String email);

    // Проверка существования пользователя по email (игнорируя регистр)
    boolean existsByEmailIgnoreCase(String email);

    // Проверка существования другого пользователя с таким же email
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u " +
            "WHERE u.email = :email AND u.id != :userId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("userId") Long userId);

    // Поиск пользователей по имени (частичное совпадение, игнорируя регистр)
    List<User> findByNameContainingIgnoreCase(String name);

    // Поиск пользователей с загрузкой их вещей
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.items WHERE u.id = :userId")
    Optional<User> findByIdWithItems(@Param("userId") Long userId);

    // Поиск пользователей с загрузкой их бронирований
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.bookings WHERE u.id = :userId")
    Optional<User> findByIdWithBookings(@Param("userId") Long userId);

    // Поиск всех пользователей с загрузкой их вещей
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.items")
    List<User> findAllWithItems();

    // Поиск пользователей с активными бронированиями
    @Query("SELECT DISTINCT u FROM User u " +
            "JOIN u.bookings b " +
            "WHERE b.status IN ('WAITING', 'APPROVED') " +
            "AND b.start <= CURRENT_TIMESTAMP " +
            "AND b.end >= CURRENT_TIMESTAMP")
    List<User> findUsersWithActiveBookings();
}