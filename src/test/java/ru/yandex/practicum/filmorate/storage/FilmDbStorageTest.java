package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@JdbcTest
@AutoConfigureTestDatabase
@Import({UserRowMapper.class, FilmRowMapper.class,
        GenreRowMapper.class, MpaRowMapper.class,
        FilmDbStorageTest.AdditionalConfigFilm.class,
        FilmDbStorageTest.AdditionalConfigUser.class,
        FilmDbStorageTest.AdditionalConfigGenre.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @TestConfiguration
    static class AdditionalConfigUser {
        @Bean
        public UserDbStorage userDbStorage(JdbcTemplate jdbcTemplate, UserRowMapper userRawMapper) {
            return new UserDbStorage(jdbcTemplate, userRawMapper);
        }
    }

    @TestConfiguration
    static class AdditionalConfigFilm {
        @Bean
        public FilmDbStorage filmDbStorage(JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper) {
            return new FilmDbStorage(jdbcTemplate, filmRowMapper);
        }
    }

    @TestConfiguration
    static class AdditionalConfigGenre {
        @Bean
        public GenreDbStorage genreDbStorage(JdbcTemplate jdbcTemplate, GenreRowMapper genreRowMapper) {
            return new GenreDbStorage(jdbcTemplate, genreRowMapper);
        }
    }

    @Test
    @DisplayName("Создание фильма инаходение его по Id")
    void testCreateAndFindById() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        Genre comedy = new Genre();
        comedy.setId(1);
        comedy.setName("Комедия");
        Genre drama = new Genre();
        drama.setId(2);
        drama.setName("Драма");
        film.setGenres(List.of(comedy, drama));

        Film created = filmStorage.createFilm(film);
        assertThat(created.getId()).isNotNull().isPositive();

        Film found = filmStorage.getFilm(created.getId()).get();
        assertThat(found.getName()).isEqualTo("Film");
        assertThat(found.getMpa().getId()).isEqualTo(1);
        assertThat(found.getGenres()).hasSize(2);
    }

    @Test
    @DisplayName("Проверка метода getFilms()")
    void testGetFilms() {
        // Изначально база должна быть пуста (если data.sql не создаёт фильмов)
        Collection<Film> emptyList = filmStorage.getFilms();
        assertThat(emptyList).isEmpty();

        // Создадим один фильм
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1999, 12, 12));
        film.setDuration(90);

        Mpa mpa = new Mpa();
        mpa.setId(2);
        mpa.setName("PG");
        film.setMpa(mpa);

        filmStorage.createFilm(film);

        Collection<Film> films = filmStorage.getFilms();
        assertThat(films).hasSize(1);
    }

    @Test
    @DisplayName("Проверка обновления фильма")
    void testUpdate() {
        Film film = new Film();
        film.setName("Old_Film");
        film.setDescription("Old_description");
        film.setReleaseDate(LocalDate.of(1980, 5, 5));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(3); // например, PG-13
        mpa.setName("PG-13");
        film.setMpa(mpa);

        Film created = filmStorage.createFilm(film);

        Film createdGet = filmStorage.getFilm(created.getId()).get();

        created.setName("New_Film");
        created.setDescription("New_description");
        created.setDuration(110);

        Mpa newMpa = new Mpa();
        newMpa.setId(4); // например, R
        newMpa.setName("R");
        created.setMpa(newMpa);

        filmStorage.updateFilm(created);

        Film updated = filmStorage.getFilm(created.getId()).get();
        assertThat(updated.getName()).isEqualTo("New_Film");
        assertThat(updated.getMpa().getId()).isEqualTo(4);
        assertThat(updated.getMpa().getName()).isEqualTo("R");
    }

    @Test
    @DisplayName("Проверка удаления фильма")
    void testDeleteFilm() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2005, 1, 1));
        film.setDuration(99);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        filmStorage.createFilm(film);

        filmStorage.deleteFilm(film.getId());

        Film finalFilm = film;
        assertThatThrownBy(() -> filmStorage.getFilm(finalFilm.getId())).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("Проверка добавления и удаления лайка")
    void testLikes() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("User_1");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userStorage.createUser(user);

        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2010, 10, 10));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(2);
        mpa.setName("PG");
        film.setMpa(mpa);

        film = filmStorage.createFilm(film);

        filmStorage.addLike(film.getId(), user.getId());

        film = filmStorage.getFilm(film.getId()).get();

        Collection<Film> popular = filmStorage.getPopularFilms(10);
        assertThat(popular).hasSize(1);
        Film popularFilm = popular.iterator().next();
        assertThat(popularFilm.getId()).isEqualTo(film.getId());

        filmStorage.removeLike(film.getId(), user.getId());
    }
}