package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {

    UserController userController = new UserController();

    User user = User.builder()
            .id(1L)
            .name("user")
            .email("email@yandex.ru")
            .login("login")
            .birthday(LocalDate.of(1983, 12, 26))
            .build();

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
        User updUser = User.builder()
                .id(1L)
                .name("updUser")
                .email("updEmail@yandex.ru")
                .login("updLogin")
                .birthday(LocalDate.of(1983, 11, 26))
                .build();
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