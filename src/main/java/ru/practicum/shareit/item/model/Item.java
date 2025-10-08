package ru.practicum.shareit.item.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * TODO Sprint add-controllers.
 */
@Data
public class Item {
    private Long id;
    private Long ownerId;

    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotNull(message = "Поле available обязательно")
    private Boolean available;
}