package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Optional<Film> getFilm(long id) {
        if (films.containsKey(id)) {
            return Optional.of(films.get(id));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Film createFilm(Film newFilm) {
        check(newFilm);
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм {} добавлен", newFilm.getName());
        return newFilm;
    }

    @Override
    public Film updateFilm(Film updFilm) {
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
        throw new NotFoundException("Фильм с id " + updFilm.getId() + " не найден");
    }

    private void check(Film film) {
        if (film.getName().isBlank()) {
            log.error("Отсутствует название фильма");
            throw new ValidationException("Должно быть указано название фильма");
        }
        if (film.getDescription().isBlank() || film.getDescription().length() > 200) {
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

