package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RespBookingDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotNull
    @FutureOrPresent(message = "Время начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull
    @Future(message = "Время завершения бронирования не может быть в прошлом")
    private LocalDateTime end;

    private ItemDto item;

    private UserDto booker;

    private BookingState status;
}
