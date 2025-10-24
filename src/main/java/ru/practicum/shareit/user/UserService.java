package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User saveUser(UserDto userDto);

    UserDto getUserById(long userId);

    UserDto patchUser(Long id, UserDto userDto);

    void deleteUser(Long userId);
}