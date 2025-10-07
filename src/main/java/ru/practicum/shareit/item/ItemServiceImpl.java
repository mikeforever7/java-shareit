package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<Item> getItems(long userId) {
        return itemRepository.findItemsByUserId(userId);
    }

    @Override
    public ItemDto getItemById(long itemId) {
        return itemRepository.findItemById(itemId)
                .map(ItemMapper::mapToItemDto)
                .orElseThrow(() -> new NotFoundException("Вещь с id =" + itemId + "не найден"));
    }

    @Override
    public Item addNewItem(Long userId, Item item) {
        if (!userRepository.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        item.setOwnerId(userId);
        return itemRepository.save(item);
    }

    @Override
    public ItemDto patchItem(Long userId, Long itemId, Item item) {
        if (!userRepository.isUserExist(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (!itemRepository.doesUserHaveItem(userId, itemId)) {
            throw new NotFoundException("У пользователя с id =" + userId + " осутствует вещь с id =" + itemId);
        }
        Item itemForPatch = itemRepository.findItemById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));
        if (StringUtils.hasText(item.getName())) {
            itemForPatch.setName(item.getName());
        }
        if (StringUtils.hasText(item.getDescription())) {
            itemForPatch.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemForPatch.setAvailable(item.getAvailable());
        }
        return ItemMapper.mapToItemDto(itemRepository.update(itemForPatch));
    }

    @Override
    public List<ItemDto> searchAvailableItemByText(String text) {
        if (!StringUtils.hasText(text)) {
            return new ArrayList<>();
        }
        String lowerCaseQuery = text.trim().toLowerCase();
        return itemRepository.searchAvailableItemByText(lowerCaseQuery);
    }

}
