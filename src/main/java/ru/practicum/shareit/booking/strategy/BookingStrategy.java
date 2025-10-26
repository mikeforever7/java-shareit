package ru.practicum.shareit.booking.strategy;

import ru.practicum.shareit.booking.model.Booking;

import java.util.List;

public interface BookingStrategy {
    List<Booking> getBookings(Long userId);
}
