package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaService {

    private final MpaDbStorage mpaDbStorage;

    public List<Mpa> getAllMpa() {
        List<Mpa> mpaList = mpaDbStorage.getAllMpa();
        mpaList.sort(Comparator.comparing(Mpa::getId));
        return mpaList;
    }

    public Mpa getMpa(Integer id) {
        return mpaDbStorage.getMpa(id).orElseThrow(() -> new NotFoundException("MPA рейтинг  с id = " + id + " не найден"));
    }
}
