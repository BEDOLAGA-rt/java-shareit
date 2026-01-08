package ru.practicum.shareit.booking.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime start;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime end;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "booker_id", nullable = false)
    private Long bookerId;

    public Long getId() {
        return id;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Long getItemId() {
        return itemId;
    }

    public Long getBookerId() {
        return bookerId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public void setBookerId(Long bookerId) {
        this.bookerId = bookerId;
    }
}