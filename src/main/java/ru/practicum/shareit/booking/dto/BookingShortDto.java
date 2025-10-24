package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.BookingState;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class BookingShortDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private BookingState status;
    private Long bookerId;
}