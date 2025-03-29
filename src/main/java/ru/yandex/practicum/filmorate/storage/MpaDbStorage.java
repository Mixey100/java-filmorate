package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mpaRowMapper;

    public List<Mpa> getAllMpa() {
        String query = "SELECT id AS mpa_id, name AS mpa_name FROM mpa";
        return jdbc.query(query, mpaRowMapper);
    }

    public Optional<Mpa> getMpa(Integer id) {
        String query = "SELECT id AS mpa_id, name AS mpa_name FROM mpa WHERE id = ?";
        try {
            Mpa result = jdbc.queryForObject(query, mpaRowMapper, id);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
