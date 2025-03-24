package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreDbStorage genreDbStorage;

    public List<Genre> getGenres() {
        List<Genre> genreList = genreDbStorage.getGenres();
        genreList.sort(Comparator.comparing(Genre::getId));
        return genreList;
    }

    public Genre getGenre(Integer id) {
        return genreDbStorage.getGenre(id).orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }
}
