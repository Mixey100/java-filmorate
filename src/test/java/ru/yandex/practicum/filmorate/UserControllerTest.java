package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {
    @Autowired

    UserController userController = new UserController(new UserService(new InMemoryUserStorage()));

    User user = new User(1L, "user", "email@yandex.ru", "login",
            LocalDate.of(1983, 12, 26));

    @Test
    public void testShouldGetUsers() {
        userController.createUser(user);
        assertEquals(1, userController.getUsers().size());
    }

    @Test
    public void testShouldCreateUser() {
        User tesUser = userController.createUser(user);
        assertEquals("user", user.getName());
        assertEquals(1, userController.getUsers().size());
    }

    @Test
    public void testShouldUpdateUser() {
        User tesUser = userController.createUser(user);
        User updUser = new User(1L, "updUser", "updEmail@yandex.ru", "updLogin",
                LocalDate.of(1983, 11, 26));
        userController.updateUser(updUser);
        assertEquals("updUser", user.getName());
        assertEquals(1, userController.getUsers().size());
    }

    @Test
    void testShouldRenameEmptyNameTologin() {
        user.setName("");
        userController.createUser(user);
        assertEquals("login", user.getName());
    }

    @Test
    void testEmailNullException() {
        user.setEmail("");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));
        assertEquals("Должен быть указан имейл", exception.getMessage());
    }

    @Test
    void testEmailNotSymbolException() {
        user.setEmail("email");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));
        assertEquals("В имейле должен содержаться символ @", exception.getMessage());
    }

    @Test
    void testLoginNullException() {
        user.setLogin("");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));
        assertEquals("Логин не должен быть пустым или содержать пробелы", exception.getMessage());
    }

    @Test
    void testBirthDayException() {
        user.setBirthday(LocalDate.now().plusYears(2));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(user));
        assertEquals("Вы из будущего", exception.getMessage());
    }
}