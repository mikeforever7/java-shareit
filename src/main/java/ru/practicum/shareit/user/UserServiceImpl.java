package ru.practicum.shareit.user;

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
public class UserServiceImpl implements UserService {
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
    public User saveUser(UserDto userDto) {
        if (repository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Пользователь с Email " + userDto.getEmail() + " существует");
        }
        return repository.save(UserMapper.mapToUser(userDto));
    }

    @Override
    public UserDto patchUser(Long id, UserDto userDto) {
        User userForPatch = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id =" + id + "не найден"));
        if (StringUtils.hasText(userDto.getName())) {
            userForPatch.setName(userDto.getName());
        }

        if (StringUtils.hasText(userDto.getEmail())) {
            Optional<User> existingUser = repository.findByEmail(userDto.getEmail());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
                throw new EmailAlreadyExistsException("Пользователь с Email " + userDto.getEmail() + " существует");
            }
            userForPatch.setEmail(userDto.getEmail());
        }
        return UserMapper.mapToUserDto(repository.save(userForPatch));
    }

    @Override
    public void deleteUser(Long userId) {
        repository.deleteById(userId);
    }
}