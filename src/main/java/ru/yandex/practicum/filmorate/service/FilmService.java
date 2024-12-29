package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage storage;
    private final UserService service;

    public FilmService(FilmStorage storage, UserService service) {
        this.storage = storage;
        this.service = service;
    }

    public Collection<Film> getFilms() {
        return storage.getFilms();
    }

    public Optional<Film> getFilm(long id) {
        return storage.getFilm(id);
    }

    public Film createFilm(Film newFilm) {
        check(newFilm);
        storage.createFilm(newFilm);
        log.info("Фильм {} добавлен", newFilm.getName());
        return newFilm;
    }

    public Film updateFilm(Film updFilm) {
        if (updFilm.getId() == null) {
            throw new ValidationException("Должен быть указан id фильма");
        }
        check(updFilm);
        Film film = storage.updateFilm(updFilm);
        if (film != null) {
            log.info("Успешное обновление фильма {}", film.getName());
            return film;
        }
        throw new NotFoundException("Фильм с id " + updFilm.getId() + " не найден");
    }

    public List<Film> getPopularFilms(long count) {
        Comparator<Film> comparator = Comparator.comparingInt(film -> film.getLikes().size());
        return getFilms()
                .stream()
                .sorted(comparator.reversed())
                .limit(count)
                .toList();
    }

    public Film addLike(long filmId, long userId) {
        Optional<Film> optFilm = getFilm(filmId);
        Optional<User> optUser = service.getUser(userId);

        optUser.orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));

        Film film = optFilm.orElseThrow(() -> new NotFoundException("Фильм с id: " + filmId + " не найден"));

        film.getLikes().add(userId);
        log.info("Пользователь с id: {} поставил лайк фильму с id: {}", userId, filmId);
        return film;
    }

    public Film deleteLike(long filmId, long userId) {
        Optional<Film> optFilm = getFilm(filmId);
        Optional<User> optUser = service.getUser(userId);

        optUser.orElseThrow(() -> new NotFoundException("Пользователь с id: " + userId + " не найден"));

        Film film = optFilm.orElseThrow(() -> new NotFoundException("Фильм с id: " + filmId + " не найден"));

        film.getLikes().remove(userId);
        log.info("Пользователь с id: {} удалил лайк у фильма с id: {}", userId, filmId);
        return film;
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
}
