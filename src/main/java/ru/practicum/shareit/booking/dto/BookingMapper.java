package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BookingMapper {
    public static RespBookingDto mapToBookingDto(Booking booking) {
        return new RespBookingDto(booking.getId(), booking.getStart(), booking.getEnd(),
                ItemMapper.mapToItemDto(booking.getItem()), UserMapper.mapToUserDto(booking.getBooker()), booking.getStatus());
    }

    public static Booking mapToBooking(ReqBookingDto reqBookingDto, User booker, Item item) {
        Booking booking = new Booking();
        booking.setBooker(booker);
        booking.setItem(item);
        booking.setStart(reqBookingDto.getStart());
        booking.setEnd(reqBookingDto.getEnd());
        return booking;
    }

    public static List<RespBookingDto> mapToBookingList(List<Booking> bookings) {
        return bookings.stream().map(BookingMapper::mapToBookingDto).toList();
    }

    public static BookingShortDto mapToBookingShortDto(Booking booking) {
        if (booking == null) {
            return null;
        }
        return new BookingShortDto(booking.getId(), booking.getStart(), booking.getEnd(),
                booking.getStatus(), booking.getBooker().getId());
    }

}
