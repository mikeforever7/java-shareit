package ru.practicum.shareit.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.request.dto.ItemRequestDto;

public class ItemRequestClient extends BaseClient {

    @Autowired
    public ItemRequestClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> saveRequest(long userId, ItemRequestDto requestDto) {
        return post("", userId, requestDto);
    }

    public ResponseEntity<Object> getUserItemRequests(long userId) {
        return get("", userId);
    }

    public ResponseEntity<Object> getAllItemRequests() {
        return get("/all");
    }

    public ResponseEntity<Object> getItemRequest(long itemRequestId) {
        return get("/" + itemRequestId);
    }
}
