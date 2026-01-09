package ru.practicum.shareit.request;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {

    // Поиск всех запросов пользователя, отсортированных по дате создания
    List<ItemRequest> findByRequesterIdOrderByCreatedDesc(Long requesterId);

    // Поиск всех запросов других пользователей, отсортированных по дате создания
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.requester.id != :requesterId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllByRequesterIdNot(@Param("requesterId") Long requesterId, Pageable pageable);

    // Поиск всех запросов других пользователей без пагинации
    @Query("SELECT ir FROM ItemRequest ir " +
            "WHERE ir.requester.id != :requesterId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findAllByRequesterIdNot(@Param("requesterId") Long requesterId);

    // Поиск запроса с загрузкой вещей - ВОТ ЗДЕСЬ ИЗМЕНЕНИЕ!
    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items " +
            "WHERE ir.id = :requestId")
    Optional<ItemRequest> findByIdWithItems(@Param("requestId") Long requestId);  // Changed to Optional

    // Поиск всех запросов с загрузкой вещей
    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items")
    List<ItemRequest> findAllWithItems();

    // Поиск запросов пользователя с загрузкой вещей
    @Query("SELECT DISTINCT ir FROM ItemRequest ir " +
            "LEFT JOIN FETCH ir.items " +
            "WHERE ir.requester.id = :requesterId " +
            "ORDER BY ir.created DESC")
    List<ItemRequest> findByRequesterIdWithItems(@Param("requesterId") Long requesterId);

    // Проверка существования запроса
    boolean existsById(Long id);

    // Проверка, является ли пользователь автором запроса
    boolean existsByIdAndRequesterId(Long id, Long requesterId);
}