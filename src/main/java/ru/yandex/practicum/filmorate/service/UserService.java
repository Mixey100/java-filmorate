package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

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

    public User getUser(long id) {
        return storage.getUser(id).orElseThrow();
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
        checkId(updUser.getId());
        check(updUser);
        User user = storage.updateUser(updUser);
        if (user != null) {
            log.info("Успешное обновление пользователя {}", user.getName());
            return user;
        } else {
            throw new NotFoundException("Неудачная попытка обновления");
        }
    }

    public void deleteUser(Long id) {
        User user = checkId(id);
        storage.deleteUser(id);
        log.info("Пользователь {} удален", user.getName());
    }

    public Collection<User> getFriends(long id) {
        checkId(id);
        return storage.getFriends(id);
    }

    public Collection<User> getCommonFriends(long userId, long friendId) {
        checkId(userId);
        checkId(friendId);
        return storage.getCommonFriends(userId, friendId);
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.error("Попытка добавить в друзья самого себя");
            throw new ValidationException("Попытка добавить в друзья самого себя");
        }
        checkId(userId);
        checkId(friendId);
        storage.addFriend(userId, friendId);
        log.info("Пользователь с id: {} добавил в друзья пользователя с id: {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.error("Попытка удалить из друзей самого себя");
            throw new ValidationException("Попытка удалить из друзей самого себя");
        }
        checkId(userId);
        checkId(friendId);
        storage.removeFriend(userId, friendId);
        log.info("Пользователь с id: {} удалил из друзей пользователя с id: {}", userId, friendId);
    }

    User checkId(Long id) {
        return storage.getUser(id).orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
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
