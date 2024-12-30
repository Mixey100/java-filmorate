package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
public class User {

    private Long id;
    private String name;
    private String email;
    private String login;
    private LocalDate birthday;
    private final Set<Long> friends = new HashSet<>();
}
