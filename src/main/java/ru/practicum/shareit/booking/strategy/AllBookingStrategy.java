package ru.practicum.shareit.booking.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AllBookingStrategy implements BookingStrategy {
    private final BookingRepository bookingRepository;

    @Override
    public List<Booking> getBookings(Long userId) {
        return bookingRepository.findByBookerIdOrderByStartDesc(userId);
    }
}
