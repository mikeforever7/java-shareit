package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    List<Item> findItemsByUserId(long userId);

    Item save(Item item);

    Item update(Item item);

    Optional<Item> findItemById(long itemId);

    boolean doesUserHaveItem(long userId, long itemId);

    List<ItemDto> searchAvailableItemByText(String text);
}
