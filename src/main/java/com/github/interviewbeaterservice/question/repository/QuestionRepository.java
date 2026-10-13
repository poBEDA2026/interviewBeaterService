package com.github.interviewbeaterservice.question.repository;

import com.github.interviewbeaterservice.question.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA-репозиторий для работы с вопросами.
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

}
