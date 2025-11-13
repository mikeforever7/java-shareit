package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentReqDto;
import ru.practicum.shareit.item.dto.ItemReqDto;
import ru.practicum.shareit.item.dto.ItemUpdateReqDto;

import java.util.Map;

public class ItemClient extends BaseClient {

    @Autowired
    public ItemClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> getItemById(long userId, long itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getItems(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> addNewItem(long userId, ItemReqDto itemDto) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> patchItem(long userId, long itemId, ItemUpdateReqDto itemUpdateReqDto) {
        return patch("/" + itemId, userId, itemUpdateReqDto);
    }

    public ResponseEntity<Object> searchAvailableItemByText(String text) {
        Map<String, Object> parameters = Map.of(
                "text", text
        );
        return get("/search?text={text}", null, parameters);
    }

    public ResponseEntity<Object> addNewComment(long userId, long itemId, CommentReqDto commentDto) {
        return post("/" + itemId + "/comment", userId, commentDto);
    }
}
