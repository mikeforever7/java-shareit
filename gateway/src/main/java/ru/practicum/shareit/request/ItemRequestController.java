package ru.practicum.shareit.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> saveNewItemRequest(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId,
                                                     @Valid @RequestBody ItemRequestDto requestDto) {
        log.info("Creating request {}", requestDto);
        return itemRequestClient.saveRequest(userId, requestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getUserItemRequests(@RequestHeader(value = "X-Sharer-User-Id", required = false) Long userId) {
        return itemRequestClient.getUserItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests() {
        return itemRequestClient.getAllItemRequests();
    }

    @GetMapping("/{itemRequestId}")
    public ResponseEntity<Object> getItemRequest(@Positive @PathVariable Long itemRequestId) {
        return itemRequestClient.getItemRequest(itemRequestId);
    }
}
