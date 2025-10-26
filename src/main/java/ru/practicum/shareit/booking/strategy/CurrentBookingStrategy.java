package ru.practicum.shareit.booking.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CurrentBookingStrategy implements BookingStrategy {
    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> getBookings(Long userId) {
        return bookingRepository.findByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId,
                LocalDateTime.now(), LocalDateTime.now());
    }
}
