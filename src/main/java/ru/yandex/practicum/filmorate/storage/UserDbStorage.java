package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    @Override
    public Collection<User> getUsers() {
        String query = "SELECT * FROM users";
        return jdbc.query(query, userRowMapper);
    }

    @Override
    public Optional<User> getUser(long id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try {
            User result = jdbc.queryForObject(query, userRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public User createUser(User newUser) {
        String query = "INSERT INTO users(name, email, login, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, newUser.getName());
            preparedStatement.setString(2, newUser.getEmail());
            preparedStatement.setString(3, newUser.getLogin());
            preparedStatement.setString(4, newUser.getBirthday().toString());
            return preparedStatement;
        }, keyHolder);
        final long userId;
        if (Objects.nonNull(keyHolder.getKey())) {
            userId = keyHolder.getKey().longValue();
            newUser.setId(userId);
        }
        return newUser;
    }

    @Override
    public User updateUser(User updUser) {
        String query = "UPDATE users SET name = ?, email = ?, login = ?, birthday = ? WHERE id = ?";
        int rows = jdbc.update(query,
                updUser.getName(),
                updUser.getEmail(),
                updUser.getLogin(),
                updUser.getBirthday(),
                updUser.getId());
        if (rows > 0) {
            return updUser;
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteUser(Long id) {
        String query = "DELETE FROM users WHERE id = ?";
        return jdbc.update(query, id) > 0;
    }

    @Override
    public Collection<User> getFriends(Long id) {
        String query = "SELECT u.* FROM friendship f " +
                "JOIN users u ON f.friend_id = u.id " +
                "WHERE f.user_id = ?";
        return jdbc.query(query, userRowMapper, id);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long friendId) {
        String query = "SELECT u.* FROM friendship f1 " +
                "JOIN friendship f2 ON f1.friend_id = f2.friend_id " +
                "JOIN users u ON f1.friend_id = u.id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbc.query(query, userRowMapper, userId, friendId);
    }

    @Override
    public boolean addFriend(Long userId, Long friendId) {
        String query = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        return jdbc.update(query, userId, friendId) > 0;
    }

    @Override
    public boolean removeFriend(Long userId, Long friendId) {
        String query = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        return jdbc.update(query, userId, friendId) > 0;
    }
}

