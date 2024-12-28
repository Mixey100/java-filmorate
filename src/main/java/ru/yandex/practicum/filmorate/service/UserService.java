package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

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
        return storage.createUser(newUser);
    }

    public User updateUser(User updUser) {
        return storage.updateUser(updUser);
    }

    public List<User> getFriends(long id) {
        Optional<User> optUser = getUser(id);
        if (optUser.isPresent()) {
            Set<Long> friendsId = optUser.get().getFriends();
            return getUsers()
                    .stream()
                    .filter(user -> friendsId.contains(user.getId()))
                    .toList();
        } else {
            logUserError(id);
            throw new NotFoundException("Пользователь с id: " + id + " не найден");
        }
    }

    public List<User> getCommonFriends(long userId, long friendId) {
        Optional<User> optUser = getUser(userId);
        Optional<User> optFriend = getUser(friendId);
        if (optUser.isEmpty()) {
            logUserError(userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден");
        } else if (optFriend.isEmpty()) {
            logUserError(friendId);
            throw new NotFoundException("Пользователь с id: " + friendId + " не найден");
        } else {
            Set<Long> userFriendsId = optUser.get().getFriends();
            Set<Long> friendFriendsId = optFriend.get().getFriends();
            List<Long> commonId = userFriendsId
                    .stream()
                    .filter(friendFriendsId::contains)
                    .toList();

            return getUsers()
                    .stream()
                    .filter(user -> commonId.contains(user.getId()))
                    .toList();
        }
    }

    public User addFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.error("Попытка добавить в друзья самого себя");
            throw new ValidationException("Попытка добавить в друзья самого себя");
        }
        Optional<User> optUser = getUser(userId);
        Optional<User> optFriend = getUser(friendId);
        if (optUser.isEmpty()) {
            logUserError(userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден");
        } else if (optFriend.isEmpty()) {
            logUserError(friendId);
            throw new NotFoundException("Пользователь с id: " + friendId + " не найден");
        } else {
            User user = optUser.get();
            User friend = optFriend.get();
            user.getFriends().add(friendId);
            friend.getFriends().add(userId);
            log.info("Пользователь с id: {} добавил в друзья пользователя с id: {}", userId, friendId);
            return user;
        }
    }

    public User deleteFriend(long userId, long friendId) {
        if (userId == friendId) {
            log.error("Попытка удалить из друзей самого себя");
            throw new ValidationException("Попытка удалить из друзей самого себя");
        }
        Optional<User> optUser = getUser(userId);
        Optional<User> optFriend = getUser(friendId);
        if (optUser.isEmpty()) {
            logUserError(userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден");
        } else if (optFriend.isEmpty()) {
            logUserError(friendId);
            throw new NotFoundException("Пользователь с id: " + friendId + " не найден");
        } else {
            User user = optUser.get();
            User friend = optFriend.get();
            user.getFriends().remove(friendId);
            friend.getFriends().remove(userId);
            log.info("Пользователь с id: {} удалил из друзей пользователя с id: {}", userId, friendId);
            return user;
        }
    }

    void logUserError(long id) {
        log.error("Пользователь с id: {} не найден", id);
    }
}
