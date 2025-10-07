package ru.practicum.shareit.user;


import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.practicum.shareit.exception.EmailAlreadyExistsException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<User> getAllUsers() {
        return repository.findAll();
    }

    @Override
    public UserDto getUserById(long id) {
        return repository.findById(id)
                .map(UserMapper::mapToUserDto)
                .orElseThrow(() -> new NotFoundException("Пользователь с id =" + id + "не найден"));
    }

    @Override
    public User saveUser(User user) {
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Пользователь с Email " + user.getEmail() + " существует");
        }
        return repository.save(user);
    }

    @Override
    public UserDto patchUser(Long id, User user) {
        User userForPatch = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id =" + id + "не найден"));
        if (StringUtils.hasText(user.getName())) {
            userForPatch.setName(user.getName());
        }

        if (StringUtils.hasText(user.getEmail())) {
            validateEmail(user.getEmail());
            Optional<User> existingUser = repository.findByEmail(user.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new EmailAlreadyExistsException("Пользователь с Email " + user.getEmail() + " существует");
            }
            userForPatch.setEmail(user.getEmail());
        }
        return UserMapper.mapToUserDto(repository.update(userForPatch));
    }

    @Override
    public void validateEmail(String email) {
        if (!(email.contains("@"))
                || !(email.contains("."))
                || email.trim().isEmpty()) {
            throw new ValidationException("Некорректный Email");
        }
    }

    @Override
    public void deleteUser(Long userId) {
        repository.delete(userId);
    }
}