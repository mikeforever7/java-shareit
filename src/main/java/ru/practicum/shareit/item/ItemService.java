package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {
    List<Item> getItems(long userId);

    ItemDto getItemById(long itemId);

    Item addNewItem(Long userId, Item item);

    ItemDto patchItem(Long userId, Long itemId, Item item);

    List<ItemDto> searchAvailableItemByText(String text);
}
