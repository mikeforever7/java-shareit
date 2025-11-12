package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addNewItemRequest(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                            @RequestBody ItemRequestDto requestDto) {
        return itemRequestService.addItemRequest(userId, requestDto);
    }

    @GetMapping
    public List<ItemRequestWithItemsDto> getUserItemRequests(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return itemRequestService.getUserItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests() {
        return itemRequestService.getAllItemRequests();
    }

    @GetMapping("/{itemRequestId}")
    public ItemRequestWithItemsDto getItemRequest(@PathVariable Long itemRequestId) {
        return itemRequestService.getItemRequest(itemRequestId);
    }
}

