package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();

    User saveUser(User user);

    UserDto getUserById(long userId);

    UserDto patchUser(Long id, User user);

    void validateEmail(String email);

    void deleteUser(Long userId);
}