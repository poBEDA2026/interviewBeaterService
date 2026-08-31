package com.github.interviewbeaterservice.answer.repository;

import com.github.interviewbeaterservice.answer.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
interface AnswerRepository extends JpaRepository<Answer, Long> {

    /**
     * Получить список ответов на вопрос
     * @param questionId ID вопроса
     * @return Список ответов
     */
    List<Answer> findAllByQuestionId(Long questionId);
}