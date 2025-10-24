package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithCommentsDto;

import java.util.List;

/**
 * TODO Sprint add-controllers.
 */

@Slf4j
@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDto> getUserItems(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemWithCommentsDto getItemById(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                           @PathVariable long itemId) {
        return itemService.getItemById(userId, itemId);
    }

    @PostMapping
    public ItemDto addItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                           @Valid @RequestBody ItemDto itemDto) {
        return itemService.addNewItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto postComment(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                  @PathVariable Long itemId, @Valid @RequestBody CommentDto commentDto) {
        return itemService.addNewComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                             @PathVariable Long itemId, @RequestBody ItemDto itemDto) {
        return itemService.patchItem(userId, itemId, itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchAvailableItemByText(@RequestParam String text) {
        return itemService.searchAvailableItemByText(text);
    }

}