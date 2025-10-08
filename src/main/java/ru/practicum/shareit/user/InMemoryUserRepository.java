package ru.practicum.shareit.user;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<String, User> usersByEmail = new HashMap<>();
    private final Map<Long, User> usersById = new HashMap<>();

    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersByEmail.values());
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    @Override
    public Optional<User> findById(long userId) {
        return Optional.ofNullable(usersById.get(userId));
    }

    @Override
    public User save(User user) {
        user.setId(getId());
        usersByEmail.put(user.getEmail(), user);
        usersById.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        usersByEmail.put(user.getEmail(), user);
        usersById.put(user.getId(), user);
        return user;
    }

    @Override
    public void delete(Long id) {
        User user = findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id =" + id + "не найден"));
        usersById.remove(id);
        usersByEmail.remove(user.getEmail());
    }

    @Override
    public boolean isUserExist(Long userId) {
        return usersById.containsKey(userId);
    }

    private long getId() {
        long lastId = usersByEmail.values().stream()
                .mapToLong(User::getId)
                .max()
                .orElse(0);
        return lastId + 1;
    }
}
