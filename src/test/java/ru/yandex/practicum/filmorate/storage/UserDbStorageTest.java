package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserRowMapper.class, UserDbStorageTest.AdditionalConfigUser.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    @TestConfiguration
    static class AdditionalConfigUser {
        @Bean
        public UserDbStorage userDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRawMapper) {
            return new UserDbStorage(jdbcTemplate, userRawMapper);
        }
    }

    @Test
    @DisplayName("Проверка метода создания пользователя и нахождение его по Id")
    void testCreateAndFindById() {
        User user = new User();
        user.setName("Name");
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userStorage.createUser(user);
        assertThat(created.getId()).isNotNull().isPositive();

        User found = userStorage.getUser(created.getId()).get();
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Name");
        assertThat(found.getEmail()).isEqualTo("user@example.com");
        assertThat(found.getLogin()).isEqualTo("login");
        assertThat(found.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    @DisplayName("Проверка метода getUsers()")
    void testGetUsers() {
        Collection<User> emptyList = userStorage.getUsers();
        assertThat(emptyList).isEmpty();

        User user1 = new User();
        user1.setName("User");
        user1.setEmail("user@example.com");
        user1.setLogin("user_1");
        user1.setBirthday(LocalDate.of(1980, 5, 10));
        userStorage.createUser(user1);

        User user2 = new User();
        user2.setEmail("user2@example.com");
        user2.setLogin("user_2");
        user2.setName("User_Two");
        user2.setBirthday(LocalDate.of(1992, 3, 15));
        userStorage.createUser(user2);

        Collection<User> users = userStorage.getUsers();
        assertThat(users).hasSize(2);
        assertThat(users).extracting("login").containsExactlyInAnyOrder("user_1", "user_2");
    }

    @Test
    @DisplayName("Проверка метода обновления пользователя")
    void testUpdateUser() {
        User user = new User();
        user.setName("Old_Name");
        user.setEmail("old@example.com");
        user.setLogin("oldlogin");
        user.setBirthday(LocalDate.of(1970, 1, 1));
        user = userStorage.createUser(user);

        user.setEmail("new@example.com");
        user.setLogin("newlogin");
        user.setName("New_Name");
        userStorage.updateUser(user);

        User updated = userStorage.getUser(user.getId()).get();
        assertThat(updated.getEmail()).isEqualTo("new@example.com");
        assertThat(updated.getLogin()).isEqualTo("newlogin");
        assertThat(updated.getName()).isEqualTo("New_Name");
    }

    @Test
    @DisplayName("Проверка метода удаления пользователя")
    void testDeleteUser() {
        User user = new User();
        user.setName("User");
        user.setEmail("user@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1999, 9, 9));
        user = userStorage.createUser(user);

        userStorage.deleteUser(user.getId());

        User finalUser = user;
        assertThatThrownBy(() -> userStorage.getUser(finalUser.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Провека методов добавления и удаления друзей")
    void testAddAndDeleteFriend() {
        User user1 = new User();
        user1.setName("User");
        user1.setEmail("user@example.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));
        user1 = userStorage.createUser(user1);

        User user2 = new User();
        user2.setName("User_2");
        user2.setEmail("user_2@example.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1991, 2, 2));
        user2 = userStorage.createUser(user2);

        userStorage.addFriend(user1.getId(), user2.getId());
        assertThat(userStorage.getFriends(user1.getId()))
                .hasSize(1)
                .extracting("id")
                .containsExactly(user2.getId());
        assertThat(userStorage.getFriends(user2.getId())).isEmpty();

        userStorage.removeFriend(user1.getId(), user2.getId());
        assertThat(userStorage.getFriends(user1.getId())).isEmpty();
    }
}

