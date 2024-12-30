package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }

    @Override
    public Optional<User> getUser(long id) {
        if (users.containsKey(id)) {
            return Optional.of(users.get(id));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public User createUser(User newUser) {
        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    @Override
    public User updateUser(User updUser) {
        if (users.containsKey(updUser.getId())) {
            User user = users.get(updUser.getId());
            user.setEmail(updUser.getEmail());
            user.setLogin(updUser.getLogin());
            user.setBirthday(updUser.getBirthday());
            return user;
        }
        return null;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

