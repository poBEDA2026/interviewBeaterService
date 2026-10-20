package com.github.interviewbeaterservice.answer.repository;

import com.github.interviewbeaterservice.answer.entity.Answer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA-репозиторий для работы с ответами на вопросы.
 */
@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

  /**
   * Получить список ответов на вопрос
   *
   * @param questionId ID вопроса
   * @return Список ответов
   */
  List<Answer> findAllByQuestionId(Long questionId);
}