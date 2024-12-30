package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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
        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    @Override
    public Film updateFilm(Film updFilm) {
        if (films.containsKey(updFilm.getId())) {
            Film film = films.get(updFilm.getId());
            film.setName(updFilm.getName());
            film.setDescription(updFilm.getDescription());
            film.setReleaseDate(updFilm.getReleaseDate());
            film.setDuration(updFilm.getDuration());
            return film;
        }
        return null;
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

