package com.github.interviewbeaterservice.question.repository;

import com.github.interviewbeaterservice.question.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    /**
     * Получить список вложений вопроса
     * @param questionId ID вопроса
     * @return Список вложений
     */
    List<Attachment> findAllByQuestionId(Long questionId);

    /**
     * Получить конкретное вложение в рамках вопроса
     * @param questionId ID вопроса
     * @param attachmentId ID вложения
     */
    Optional<Attachment> findByQuestionIdAndId(Long questionId, Long attachmentId);
}
