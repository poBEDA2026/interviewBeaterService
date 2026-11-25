package com.github.interviewbeaterservice.question.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сложность вопроса, соответствующая грейду: junior, middle, middle+, senior.=
 * Поле level задаёт порядок возрастания сложности (1 — junior, ... 4 — senior).
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
@Table(name = "difficulties")
public class Difficulty {

    @Builder
    Difficulty(String name, Integer level) {
        this.name = name;
        this.level = level;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String name;

    @Column(nullable = false, unique = true)
    private Integer level;
}
