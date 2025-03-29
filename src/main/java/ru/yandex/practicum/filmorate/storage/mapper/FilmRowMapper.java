package ru.yandex.practicum.filmorate.storage.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {

    private final MpaRowMapper mpaRowMapper;
    private final GenreDbStorage genreDbStorage;

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("film_id"));
        film.setName(resultSet.getString("film_name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getDate("release_date").toLocalDate());
        film.setDuration(resultSet.getInt("duration"));

        Mpa mpa = mpaRowMapper.mapRow(resultSet, rowNum);
        film.setMpa(mpa);

        List<Genre> genres = genreDbStorage.getGenresByFilmId(film.getId());
        film.setGenres(genres);
        return film;
    }
}

