package ru.practicum.shareit.request.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemRequestMapper {

    public static ItemRequest mapToItemRequest(ItemRequestDto requestDto, User requester) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(requestDto.getDescription());
        itemRequest.setRequester(requester);
        return itemRequest;
    }

    public static ItemRequestDto mapToDto(ItemRequest itemRequest) {
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription(itemRequest.getDescription());
        dto.setRequesterId(itemRequest.getRequester().getId());
        dto.setId(itemRequest.getId());
        dto.setCreated(itemRequest.getCreated());
        return dto;
    }

    public static ItemRequestWithItemsDto mapToDtoWithItems(ItemRequest itemRequest) {
        List<Item> items = itemRequest.getItems();
        ItemRequestWithItemsDto dto = new ItemRequestWithItemsDto();
        dto.setDescription(itemRequest.getDescription());
        dto.setRequesterId(itemRequest.getRequester().getId());
        dto.setId(itemRequest.getId());
        dto.setCreated(itemRequest.getCreated());
        dto.setItems(ItemMapper.mapToItemForRequestDtoList(items));
        return dto;
    }

    public static List<ItemRequestWithItemsDto> mapToListDtoWithItems(List<ItemRequest> requests) {
        return requests.stream().map(ItemRequestMapper::mapToDtoWithItems).toList();
    }

    public static List<ItemRequestDto> mapToListDto(List<ItemRequest> requests) {
        return requests.stream().map(ItemRequestMapper::mapToDto).toList();
    }
}
