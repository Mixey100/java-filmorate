package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @GetMapping
    public Collection<User> getUsers() {
        return users.values();
    }

    @PostMapping
    public User createUser(User newUser) {
        check(newUser);
        if (newUser.getName() == null || newUser.getName().isBlank()) {
            newUser.setName(newUser.getLogin());
        }
        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь {} добавлен", newUser.getName());
        return newUser;
    }

    @PutMapping
    public User updateUser(User updUser) {
        if (updUser.getId() == null) {
            log.error("Отсутствует id пользователя");
            throw new ValidationException("Должен быть указан id пользователя");
        }
        if (users.containsKey(updUser.getId())) {
            check(updUser);
            User user = users.get(updUser.getId());
            if (updUser.getName() == null || updUser.getName().isBlank()) {
                user.setName(updUser.getLogin());
            } else {
                user.setName(updUser.getName());
            }
            user.setEmail(updUser.getEmail());
            user.setLogin(updUser.getLogin());
            user.setBirthday(updUser.getBirthday());
            log.info("Успешное обновление пользователя {}", user.getName());
            return user;
        }
        log.error("Пользователь с id {} не найден", updUser.getId());
        throw new ValidationException("Пользователь с id " + updUser.getId() + " не найден");
    }

    private void check(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Имейл не введен");
            throw new ValidationException("Должен быть указан имейл");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Имейл не содержит символ @");
            throw new ValidationException("В имейле должен содержаться символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.error("Логин пустой или содержит пробелы");
            throw new ValidationException("Логин не должен быть пустым или содержать пробелы");
        }
        if (user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Вы из будущего");
            throw new ValidationException("Вы из будущего");
        }
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
