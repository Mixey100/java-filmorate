package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Collection<User> getUsers();

    Optional<User> getUser(long id);

    User createUser(User newUser);

    User updateUser(User updUser);

    boolean deleteUser(Long id);

    Collection<User> getFriends(Long id);

    Collection<User> getCommonFriends(Long userId, Long friendId);

    boolean addFriend(Long userId, Long friendId);

    boolean removeFriend(Long userId, Long friendId);


}
