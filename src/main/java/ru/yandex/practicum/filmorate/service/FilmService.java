package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

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

    public Film getFilm(Long id) {
        return storage.getFilm(id).orElseThrow();
    }

    public Film createFilm(Film newFilm) {
        check(newFilm);
        Film film = storage.createFilm(newFilm);
        log.info("Фильм {} успешно создан", newFilm.getName());
        return film;
    }

    public Film updateFilm(Film updFilm) {
        check(updFilm);
        Film film = storage.updateFilm(updFilm);
        if (film != null) {
            log.info("Успешное обновление фильма {}", film.getName());
            return film;
        }
        throw new NotFoundException("Фильм с id " + updFilm.getId() + " не найден");
    }

    public void deleteFilm(Long id) {
        Film film = checkId(id);
        storage.deleteFilm(id);
        log.info("Фильм {} удален", film.getName());
    }

    public List<Film> getPopularFilms(int count) {
        return storage.getPopularFilms(count);
    }

    public void addLike(Long filmId, Long userId) {
        checkId(filmId);
        service.checkId(userId);
        if (storage.addLike(filmId, userId)) {
            log.info("Пользователь с id: {} поставил лайк фильму с id: {}", userId, filmId);
        } else {
            throw new ValidationException("Лайк фильму c id " + filmId + " пользователем " + userId + " уже имеется");
        }
    }

    public void deleteLike(long filmId, long userId) {
        checkId(filmId);
        service.checkId(userId);
        storage.addLike(filmId, userId);
        log.info("Пользователь с id: {} удалил лайк у фильма с id: {}", userId, filmId);
    }

    private Film checkId(Long id) {
        return storage.getFilm(id).orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
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
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("Должен быть указан рейтинг Мра");
        }
    }
}
