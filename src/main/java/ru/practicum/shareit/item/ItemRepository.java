package ru.practicum.shareit.item;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findItemsByOwnerId(long userId);

    @Query("select i from Item i left join fetch i.comments where i.id = ?1")
    Optional<Item> findByIdWithComments(Long itemId);

    Optional<Item> findItemById(long itemId);

    Optional<Item> findByIdAndOwnerId(long itemId, long userId);

    @Query("select it from Item as it where it.available = true and" +
            " (lower(it.description) like concat('%', ?1, '%') or " +
            "lower(it.name) like concat('%', ?1, '%'))")
    List<Item> searchAvailableItemByText(String text);
}
