package com.github.interviewbeaterservice.question.repository;

import com.github.interviewbeaterservice.question.entity.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий справочника сложностей вопросов.
 */
@Repository
public interface DifficultyRepository extends JpaRepository<Difficulty, Long> {

    Optional<Difficulty> findByName(String name);
}
