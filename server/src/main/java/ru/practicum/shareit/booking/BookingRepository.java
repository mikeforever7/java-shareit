package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingState status);

    List<Booking> findByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long userId, LocalDateTime startBefore, LocalDateTime endAfter);

    List<Booking> findByBookerIdAndEndBefore(Long userId, LocalDateTime endBefore);

    List<Booking> findByBookerIdAndStartAfter(Long userId, LocalDateTime startAfter);

    List<Booking> findByItemOwnerIdOrderByStartDesc(Long ownerId);

    List<Booking> findByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(Long ownerId,
                                                                             LocalDateTime startBefore, LocalDateTime endAfter);

    List<Booking> findByItemOwnerIdAndEndBefore(Long ownerId, LocalDateTime endBefore);

    List<Booking> findByItemOwnerIdAndStartAfter(Long ownerId, LocalDateTime startAfter);

    List<Booking> findByItemOwnerIdAndStatusOrderByStartDesc(Long ownerId, BookingState status);

    Optional<Booking> findByBookerIdAndItemId(Long bookerId, Long itemId);

    Optional<Booking> findFirstByItemIdAndStartBeforeOrderByStartDesc(Long itemId, LocalDateTime now);

    Optional<Booking> findFirstByItemIdAndStartAfterOrderByStartAsc(Long itemId, LocalDateTime now);
}