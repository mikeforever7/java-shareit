package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ItemUpdateReqDto {

    private Long ownerId;

    private String name;

    private String description;

    private Boolean available;
}