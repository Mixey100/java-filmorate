package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilmControllerTest {

    FilmController filmController = new FilmController(new FilmService(new InMemoryFilmStorage(),
            new UserService(new InMemoryUserStorage())));

    Film film = new Film(1L, "Film", "Film_description",
            LocalDate.of(2021, 12, 28), 130);

    @Test
    public void testShouldGetFilms() {
        filmController.createFilm(film);
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    public void testShouldCreateFilm() {
        Film testFilm = filmController.createFilm(film);
        assertEquals("Film", film.getName());
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    public void testShouldUpdateFilm() {
        Film testFilm = filmController.createFilm(film);
        Film updFilm = new Film(1L, "updFilm", "updFilm_description",
                LocalDate.of(2021, 11, 28), 130);
        filmController.updateFilm(updFilm);
        assertEquals("updFilm", film.getName());
        assertEquals(1, filmController.getFilms().size());
    }

    @Test
    void testNameNullException() {
        film.setName("");
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertEquals("Должно быть указано название фильма", exception.getMessage());
    }

    @Test
    void testDescriptionLengthException() {
        film.setDescription("d".repeat(201));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertEquals("Слишком длинное описание фильма", exception.getMessage());
    }

    @Test
    void testEarlyReleaseDateException() {
        film.setReleaseDate(LocalDate.of(1894, 11, 28));
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertEquals("Неверная дата релиза", exception.getMessage());
    }

    @Test
    void durationExceptionTest() {
        film.setDuration(-100);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> filmController.createFilm(film));
        assertEquals("Продолжительность фильма должна быть положительным числом", exception.getMessage());
    }
}