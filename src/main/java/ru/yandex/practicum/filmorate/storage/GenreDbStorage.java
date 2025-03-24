package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreRowMapper;

    public List<Genre> getGenres() {
        String query = "SELECT * FROM genres";
        return jdbc.query(query, genreRowMapper);
    }

    public Optional<Genre> getGenre(Integer id) {
        String query = "SELECT * FROM genres WHERE id = ?";
        try {
            Genre result = jdbc.queryForObject(query, genreRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<Genre> getGenresByFilmId(Long filmId) {
        String sql = "SELECT g.id, g.name " +
                "FROM genres g " +
                "JOIN film_genres fg ON g.id = fg.genre_id " +
                "WHERE fg.film_id = ?";
        return jdbc.query(sql, genreRowMapper, filmId);
    }

    public Genre create(Genre genre) {
        String query = "INSERT INTO genres (name) VALUES (?)";
        jdbc.update(query, genre.getName());
        return genre;
    }
}
