package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> getFilms();

    Optional<Film> getFilm(long id);

    Film createFilm(Film newFilm);

    Film updateFilm(Film updFilm);
}
