package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> getFilms();

    Optional<Film> getFilm(Long id);

    Film createFilm(Film newFilm);

    Film updateFilm(Film updFilm);

    boolean deleteFilm(Long id);

    List<Film> getPopularFilms(int count);

    boolean addLike(Long filmId, Long userId);

    boolean removeLike(Long filmId, Long userId);
}
