package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReqBookingDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private LocalDateTime start;

    private LocalDateTime end;

    private Long itemId;
}
