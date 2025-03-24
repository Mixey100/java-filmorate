package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;

    @Override
    public Collection<Film> getFilms() {
        String query = "SELECT f.id AS film_id, " +
                "f.name AS film_name, " +
                "f.description, " +
                "f.release_date, " +
                "f.duration, " +
                "m.id AS mpa_id, " +
                "m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.id ";
        return jdbc.query(query, filmRowMapper);
    }

    @Override
    public Optional<Film> getFilm(Long id) {
        String query = "SELECT f.id AS film_id, " +
                "f.name AS film_name, " +
                "f.description, " +
                "f.release_date, " +
                "f.duration, " +
                "m.id AS mpa_id, " +
                "m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.id " +
                "WHERE f.id = ?";
        try {
            Film result = jdbc.queryForObject(query, filmRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Film createFilm(Film newFilm) {
        if (!isMpaExists(newFilm.getMpa().getId())) {
            throw new NotFoundException("Рейтинга MPA с id " + newFilm.getMpa().getId() + " не существует ");
        }
        for (Genre genre : newFilm.getGenres()) {
            if (!isGenreExists(genre.getId())) {
                throw new NotFoundException("Жанрв с id " + genre.getId() + " не существует");
            }
        }
        String query = "INSERT INTO films(name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, newFilm.getName());
            preparedStatement.setString(2, newFilm.getDescription());
            preparedStatement.setString(3, newFilm.getReleaseDate().toString());
            preparedStatement.setInt(4, newFilm.getDuration());
            preparedStatement.setInt(5, newFilm.getMpa().getId());
            return preparedStatement;
        }, keyHolder);
        final long filmId;
        if (Objects.nonNull(keyHolder.getKey())) {
            filmId = keyHolder.getKey().longValue();
            newFilm.setId(filmId);
        }
        addFilmGenres(newFilm);
        return newFilm;
    }

    @Override
    public Film updateFilm(Film updFilm) {
        String query = "Update films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE id = ?";
        int rows = jdbc.update(query,
                updFilm.getName(),
                updFilm.getDescription(),
                updFilm.getReleaseDate(),
                updFilm.getDuration(),
                updFilm.getMpa().getId(),
                updFilm.getId());
        if (rows > 0) {
            deleteFilmGenres(updFilm);
            addFilmGenres(updFilm);
            return updFilm;
        } else {
            return null;
        }
    }

    @Override
    public boolean deleteFilm(Long id) {
        String query = "DELETE FROM films WHERE id = ?";
        return jdbc.update(query, id) > 0;
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String query = "SELECT f.id AS film_id, " +
                "f.name AS film_name, " +
                "f.description, " +
                "f.release_date, " +
                "f.duration, " +
                "m.id AS mpa_id, " +
                "m.name AS mpa_name, " +
                "COUNT(l.user_id) AS like_count " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, m.id, m.name " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";
        return jdbc.query(query, filmRowMapper, count);
    }

    @Override
    public boolean addLike(Long filmId, Long userId) {
        String checkQuery = "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbc.queryForObject(checkQuery, Integer.class, filmId, userId);
        if (count == null || count == 0) {
            String query = "INSERT INTO likes (film_Id, user_id) VALUES (?, ?)";
            int countInsert = jdbc.update(query, filmId, userId);
            Film film = getFilm(filmId).orElseThrow();
            film.getLikes().add(userId);
            return countInsert > 0;
        }
        return false;
    }

    @Override
    public boolean removeLike(Long filmId, Long userId) {
        String query = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int count = jdbc.update(query, filmId, userId);
        Film film = getFilm(filmId).orElseThrow();
        film.getLikes().remove(userId);
        return count > 0;

    }

    private void addFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String checkQuery = "SELECT COUNT(*) FROM film_genres WHERE film_id = ? AND genre_id = ?";
            String insertQuery = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

            for (Genre genre : film.getGenres()) {
                Integer count = jdbc.queryForObject(checkQuery, Integer.class, film.getId(), genre.getId());
                if (count == null || count == 0) {
                    jdbc.update(insertQuery, film.getId(), genre.getId());
                }
            }
        }
    }

    private void deleteFilmGenres(Film film) {
        String delQuery = "DELETE FROM film_genres WHERE film_id = ?";
        jdbc.update(delQuery, film.getId());
    }

    private boolean isMpaExists(int mpaId) {
        String query = "SELECT COUNT(*) FROM mpa WHERE id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, mpaId);
        return count != null && count > 0;
    }

    private boolean isGenreExists(int genreId) {
        String query = "SELECT COUNT(*) FROM genres WHERE id = ?";
        Integer count = jdbc.queryForObject(query, Integer.class, genreId);
        return count != null && count > 0;
    }

}
