package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Slf4j
@Repository
public class InMemoryItemRepository implements ItemRepository {
    private final Map<Long, List<Item>> itemsByUser = new HashMap<>();
    private final Map<Long, Item> itemsById = new HashMap<>();
    private long currentId = 0;

    @Override
    public List<Item> findItemsByUserId(long userId) {
        return itemsByUser.get(userId);
    }

    @Override
    public Optional<Item> findItemById(long itemId) {
        return Optional.ofNullable(itemsById.get(itemId));
    }

    @Override
    public Item save(Item item) {
        item.setId(getId());
        if (!itemsByUser.containsKey(item.getOwnerId())) {
            itemsByUser.put(item.getOwnerId(), new ArrayList<>());
        }
        itemsByUser.get(item.getOwnerId()).add(item);
        itemsById.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        itemsByUser.get(item.getOwnerId()).add(item);
        itemsById.put(item.getId(), item);
        return item;
    }

    @Override
    public boolean doesUserHaveItem(long userId, long itemId) {
        return itemsByUser.get(userId).stream().anyMatch(item -> item.getId().equals(itemId));
    }

    public List<ItemDto> searchAvailableItemByText(String text) {
        List<ItemDto> result = new ArrayList<>();
        for (Item item : itemsById.values()) {
            if ((item.getName().toLowerCase().contains(text) || item.getDescription().toLowerCase().contains(text)) && item.getAvailable().equals(true)) {
                result.add(ItemMapper.mapToItemDto(item));
            }
        }
        return result;
    }

    private long getId() {
        return ++currentId;
    }
}
