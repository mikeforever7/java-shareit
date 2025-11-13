package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.InvalidUserRequestException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;

    @Override
    public ItemRequestDto addItemRequest(Long userId, ItemRequestDto requestDto) {
        if (userId == null) {
            throw new InvalidUserRequestException();
        }
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        ItemRequest itemRequest = itemRequestRepository.save(ItemRequestMapper.mapToItemRequest(requestDto, requester));
        return ItemRequestMapper.mapToDto(itemRequest);
    }

    @Override
    public List<ItemRequestWithItemsDto> getUserItemRequests(Long userId) {
        if (userId == null) {
            throw new InvalidUserRequestException();
        }
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }

        List<ItemRequest> requests = itemRequestRepository.findUserRequestWithAnswers(userId);
        return ItemRequestMapper.mapToListDtoWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests() {
        List<ItemRequest> requests = itemRequestRepository.findAllByOrderByCreatedDesc();
        return ItemRequestMapper.mapToListDto(requests);
    }

    @Override
    public ItemRequestWithItemsDto getItemRequest(Long itemRequestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(itemRequestId)
                .orElseThrow(() -> new NotFoundException("Запрос с id=" + itemRequestId + " не найден"));
        return ItemRequestMapper.mapToDtoWithItems(itemRequest);
    }

}