package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getItems(Long userId);

    ItemWithCommentsDto getItemById(Long userId, Long itemId);

    ItemDto addNewItem(Long userId, ItemDto itemDto);

    ItemDto patchItem(Long userId, Long itemId, ItemDto itemDto);

    List<ItemDto> searchAvailableItemByText(String text);

    CommentDto addNewComment(Long userId, Long itemId, CommentDto commentDto);
}
