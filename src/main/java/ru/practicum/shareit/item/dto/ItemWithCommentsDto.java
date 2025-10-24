package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import ru.practicum.shareit.booking.dto.BookingShortDto;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ItemWithCommentsDto {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    private Long ownerId;

    private String name;

    private String description;

    private Boolean available;

    private List<CommentDto> comments = new ArrayList<>();

    private BookingShortDto lastBooking;
    private BookingShortDto nextBooking;

}
