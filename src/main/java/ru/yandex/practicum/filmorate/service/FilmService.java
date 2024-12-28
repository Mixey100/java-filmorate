package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

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
        return storage.createFilm(newFilm);
    }

    public Film updateFilm(Film updFilm) {
        return storage.updateFilm(updFilm);
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
        if (optUser.isEmpty()) {
            service.logUserError(userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден");
        } else if (optFilm.isEmpty()) {
            logFilmError(filmId);
            throw new NotFoundException("Фильм с id: " + filmId + " не найден");
        } else {
            Film film = optFilm.get();
            film.getLikes().add(userId);
            log.info("Пользователь с id: {} поставил лайк фильму с id: {}", userId, filmId);
            return optFilm.get();
        }
    }

    public Film deleteLike(long filmId, long userId) {
        Optional<Film> optFilm = getFilm(filmId);
        Optional<User> optUser = service.getUser(userId);
        if (optUser.isEmpty()) {
            service.logUserError(userId);
            throw new NotFoundException("Пользователь с id: " + userId + " не найден");
        } else if (optFilm.isEmpty()) {
            logFilmError(filmId);
            throw new NotFoundException("Фильм с id: " + filmId + " не найден");
        } else {
            Film film = optFilm.get();
            film.getLikes().remove(userId);
            log.info("Пользователь с id: {} удалил лайк у фильма с id: {}", userId, filmId);
            return optFilm.get();
        }
    }

    private void logFilmError(long id) {
        log.error("Фьльм с id: {} не найден", id);
    }
}
