package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.dto.UserUpdateReqDto;

public class UserClient extends BaseClient {

    @Autowired
    public UserClient(RestTemplate restTemplate) {
        super(restTemplate);
    }

    public ResponseEntity<Object> getUserById(long id) {
        return get("/" + id);
    }

    public ResponseEntity<Object> getAllUsers() {
        return get("");
    }

    public ResponseEntity<Object> saveUser(UserRequestDto userDto) {
        return post("", userDto);
    }

    public ResponseEntity<Object> patchUser(long id, UserUpdateReqDto userDto) {
        return patch("/" + id, userDto);
    }

    public ResponseEntity<Object> deleteUser(long id) {
        return delete("/" + id);
    }
}
