package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // Поиск всех вещей владельца с пагинацией
    List<Item> findByOwnerId(Long ownerId, Pageable pageable);

    // Поиск всех вещей владельца без пагинации
    List<Item> findByOwnerId(Long ownerId);

    // Поиск доступных вещей по тексту в названии или описании
    @Query("SELECT i FROM Item i " +
            "WHERE i.available = true " +
            "AND (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%')))")
    List<Item> searchAvailableItems(@Param("text") String text, Pageable pageable);

    // Поиск вещей по запросу
    List<Item> findByRequestId(Long requestId);

    // Поиск вещей по нескольким запросам
    List<Item> findByRequestIdIn(List<Long> requestIds);

    // Проверка существования вещи по ID и владельцу
    boolean existsByIdAndOwnerId(Long itemId, Long ownerId);

    // Получение вещи с владельцем
    @Query("SELECT i FROM Item i JOIN FETCH i.owner WHERE i.id = :itemId")
    Item findByIdWithOwner(@Param("itemId") Long itemId);

    // Получение всех вещей с их владельцами
    @Query("SELECT DISTINCT i FROM Item i JOIN FETCH i.owner")
    List<Item> findAllWithOwners();
}