package ru.yandex.practicum.filmorate.controller;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(FilmController.class);

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@RequestBody Film newFilm) {
        check(newFilm);
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм {} добавлен", newFilm.getName());
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film updFilm) {
        if (updFilm.getId() == null) {
            log.error("Отсутствует id фильма");
            throw new ValidationException("Должен быть указан id фильма");
        }
        if (films.containsKey(updFilm.getId())) {
            check(updFilm);
            Film film = films.get(updFilm.getId());
            film.setName(updFilm.getName());
            film.setDescription(updFilm.getDescription());
            film.setReleaseDate(updFilm.getReleaseDate());
            film.setDuration(updFilm.getDuration());
            log.info("Успешное обновление фильма {}", film.getName());
            return film;
        }
        log.error("Фильм с id {} не найден", updFilm.getId());
        throw new ValidationException("Фильм с id " + updFilm.getId() + " не найден");
    }

    private void check(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Отсутствует название фильма");
            throw new ValidationException("Должно быть указано название фильма");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.error("Слишком длинное описание фильма");
            throw new ValidationException("Слишком длинное описание фильма");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.error("Неверная дата релиза");
            throw new ValidationException("Неверная дата релиза");
        }
        if (film.getDuration() == null || film.getDuration() < 0) {
            log.error("Продолжительность фильма - отрицательное число");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
