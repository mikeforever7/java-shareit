package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.error.InvalidUserRequestException;
import ru.practicum.shareit.item.dto.CommentReqDto;
import ru.practicum.shareit.item.dto.ItemReqDto;
import ru.practicum.shareit.item.dto.ItemUpdateReqDto;


@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/items")
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @GetMapping
    public ResponseEntity<Object> getUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        if (userId == null) {
            throw new InvalidUserRequestException("Пользователь не авторизован");
        }
        return itemClient.getItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                              @PathVariable long itemId) {
        if (userId == null) {
            throw new InvalidUserRequestException("Пользователь не авторизован");
        }
        return itemClient.getItemById(userId, itemId);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                          @Valid @RequestBody ItemReqDto itemDto) {
        log.info("Creating item {}, userId={}", itemDto, userId);
        if (userId == null) {
            throw new InvalidUserRequestException("Пользователь не авторизован");
        }
        return itemClient.addNewItem(userId, itemDto);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                              @PathVariable Long itemId, @Valid @RequestBody CommentReqDto commentDto) {
        if (userId == null) {
            throw new InvalidUserRequestException("Пользователь не авторизован");
        }
        return itemClient.addNewComment(userId, itemId, commentDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> patchItem(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                            @PathVariable Long itemId, @RequestBody ItemUpdateReqDto itemDto) {
        if (userId == null) {
            throw new InvalidUserRequestException("Пользователь не авторизован");
        }
        return itemClient.patchItem(userId, itemId, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchAvailableItemByText(@RequestParam String text) {
        return itemClient.searchAvailableItemByText(text);
    }

}
