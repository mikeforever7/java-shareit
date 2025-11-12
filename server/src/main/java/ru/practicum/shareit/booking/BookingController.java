package ru.practicum.shareit.booking;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.ReqBookingDto;
import ru.practicum.shareit.booking.dto.RespBookingDto;

import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public RespBookingDto addBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                     @Valid @RequestBody ReqBookingDto reqBookingDto) {
        return bookingService.addNewBooking(userId, reqBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public RespBookingDto patchBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                       @PathVariable Long bookingId, @RequestParam Boolean approved) {
        return bookingService.patchBooking(userId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public RespBookingDto getBooking(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                     @PathVariable Long bookingId) {
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<RespBookingDto> getUserBookings(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                                @RequestParam(defaultValue = "ALL") RequestState state) {
        return bookingService.getBookingsByUserAndState(userId, state);
    }

    @GetMapping("/owner")
    public List<RespBookingDto> getOwnerBookings(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                                 @RequestParam(defaultValue = "ALL") RequestState state) {
        return bookingService.getBookingsByOwnerAndState(userId, state);
    }

}
