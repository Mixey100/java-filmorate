package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class User {

    private Long id;
    private String name;
    private String email;
    private String login;
    private LocalDate birthday;
    private final Set<Long> friends = new HashSet<>();
}
