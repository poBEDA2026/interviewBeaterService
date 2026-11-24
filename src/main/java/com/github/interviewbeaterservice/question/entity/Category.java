package com.github.interviewbeaterservice.question.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Категория вопроса.
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@ToString(of = {"id", "name"})
@Table(name = "categories")
public class Category {

    @Builder
    Category(String name) {
        this.name = name;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;
}
