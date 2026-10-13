package com.github.interviewbeaterservice.answer.repository;

import com.github.interviewbeaterservice.answer.entity.Answer;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

  /**
   * Получить список ответов на вопрос
   *
   * @param questionId ID вопроса
   * @return Список ответов
   */
  List<Answer> findAllByQuestionId(Long questionId);

  /**
   * Найти правильный ответ на вопрос
   *
   * @param questionId ID вопроса
   * @return Optional с правильным ответом
   */
  Optional<Answer> findFirstByQuestionIdAndIsCorrectTrue(Long questionId);
}