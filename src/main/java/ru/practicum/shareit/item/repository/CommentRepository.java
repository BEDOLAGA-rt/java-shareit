package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Получение всех комментариев для вещи
    List<Comment> findByItemId(Long itemId);

    // Получение комментариев для списка вещей
    @Query("SELECT c FROM Comment c JOIN FETCH c.author " +
            "WHERE c.item.id IN :itemIds " +
            "ORDER BY c.created DESC")
    List<Comment> findByItemIds(@Param("itemIds") List<Long> itemIds);

    // Получение комментариев с авторами для вещи
    @Query("SELECT c FROM Comment c JOIN FETCH c.author " +
            "WHERE c.item.id = :itemId " +
            "ORDER BY c.created DESC")
    List<Comment> findByItemIdWithAuthor(@Param("itemId") Long itemId);

    // Проверка существования комментария пользователя для вещи
    boolean existsByItemIdAndAuthorId(Long itemId, Long authorId);

    // Получение комментариев по автору
    List<Comment> findByAuthorId(Long authorId);
}