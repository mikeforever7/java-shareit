package ru.practicum.shareit.booking;


import ru.practicum.shareit.booking.dto.ReqBookingDto;
import ru.practicum.shareit.booking.dto.RespBookingDto;

import java.util.List;


public interface BookingService {

    RespBookingDto addNewBooking(Long userId, ReqBookingDto reqBookingDto);

    RespBookingDto patchBooking(Long userId, Long bookingId, Boolean approved);

    RespBookingDto getBooking(Long userId, Long bookingId);

    List<RespBookingDto> getBookingsByUserAndState(Long userId, RequestState state);

}
