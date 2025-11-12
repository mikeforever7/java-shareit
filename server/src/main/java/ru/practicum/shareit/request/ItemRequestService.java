package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto addItemRequest(Long userId, ItemRequestDto requestDto);

    List<ItemRequestWithItemsDto> getUserItemRequests(Long userId);

    List<ItemRequestDto> getAllItemRequests();

    ItemRequestWithItemsDto getItemRequest(Long itemRequestId);
}
