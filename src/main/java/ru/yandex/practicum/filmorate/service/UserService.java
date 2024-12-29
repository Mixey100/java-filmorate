package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private final UserStorage storage;

    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public Collection<User> getUsers() {
        return storage.getUsers();
    }

    public Optional<User> getUser(long id) {
        return storage.getUser(id);
    }

    public User createUser(User newUser) {
        check(newUser);
        if (newUser.getName() == null || newUser.getName().isEmpty()) {
            newUser.setName(newUser.getLogin());
        }
        storage.createUser(newUser);
        log.info("Пользователь {} добавлен", newUser.getName());
        return newUser;
    }

    public User updateUser(User updUser) {
        if (updUser.getId() == null) {
            throw new ValidationException("Должен быть указан id пользователя");
        }
        check(updUser);
        User user = storage.updateUser(updUser);
        if (user != null) {
            if (updUser.getName().isBlank()) {
                user.setName(updUser.getLogin());
            } else {
                user.setName(updUser.getName());
            }
            log.info("Успешное обновление пользователя {}", user.getName());
            return user;
        }
        throw new NotFoundException("Пользователь с id " + updUser.getId() + " не найден");
    }


    public List<User> getFriends(long id) {
        Optional<User> optUser = getUser(id);

        User user = optUser.orElseThrow(() -> new NotFoundException("Пользователь с id: " + id + " не найден"));

        Set<Long> friendsId = user.getFriends();
        return getUsers()
                .stream()
                .filter(currentUser -> friendsId.contains(currentUser.getId()))
                .toList();
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        Optional<User> optUser = getUser(userId);
        Optional<User> optFriend = getUser(friendId);

        User user = optUser.orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        User friend = optFriend.orElseThrow(() -> new NotFoundException("Пользователь с id: " + friendId + " не найден"));

        Set<Long> userFriendsId = user.getFriends();
        Set<Long> friendFriendsId = friend.getFriends();
        List<Long> commonId = userFriendsId
                .stream()
                .filter(friendFriendsId::contains)
                .toList();

        return getUsers()
                .stream()
                .filter(currentUser -> commonId.contains(currentUser.getId()))
                .toList();
    }

    public User addFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.error("Попытка добавить в друзья самого себя");
            throw new ValidationException("Попытка добавить в друзья самого себя");
        }
        Optional<User> optUser = getUser(userId);
        Optional<User> optFriend = getUser(friendId);

        User user = optUser.orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        User friend = optFriend.orElseThrow(() -> new NotFoundException("Пользователь с id: " + friendId + " не найден"));

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь с id: {} добавил в друзья пользователя с id: {}", userId, friendId);
        return user;
    }

    public User deleteFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.error("Попытка удалить из друзей самого себя");
            throw new ValidationException("Попытка удалить из друзей самого себя");
        }
        Optional<User> optUser = getUser(userId);
        Optional<User> optFriend = getUser(friendId);

        User user = optUser.orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));
        User friend = optFriend.orElseThrow(() -> new NotFoundException("Пользователь с id: " + friendId + " не найден"));

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь с id: {} удалил из друзей пользователя с id: {}", userId, friendId);
        return user;
    }

    private void check(User user) {
        if (user.getEmail().isBlank()) {
            log.error("Имейл не введен");
            throw new ValidationException("Должен быть указан имейл");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Имейл не содержит символ @");
            throw new ValidationException("В имейле должен содержаться символ @");
        }
        if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Логин пустой или содержит пробелы");
            throw new ValidationException("Логин не должен быть пустым или содержать пробелы");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Вы из будущего");
            throw new ValidationException("Вы из будущего");
        }
    }
}
