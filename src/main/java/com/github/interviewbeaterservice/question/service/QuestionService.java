package com.github.interviewbeaterservice.question.service;

import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    /**
     * Получение вопроса по идентификатору
     * @param id идентификатор вопроса
     * @return объект типа {@link Question}
     */
    @Transactional(readOnly = true)
    public Question getById(Long id) {
        return questionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Question %s not found".formatted(id)));
    }

    /**
     * Получение списка всех вопросов
     * @return список объектов типа {@link Question}
     */
    @Transactional(readOnly = true)
    public List<Question> getAll() {
        return questionRepository.findAll();
    }

    /**
     * Создание нового вопроса
     * @param title заголовок вопроса
     * @return новый объект типа {@link Question}
     */
    @Transactional
    public Question create(String title) {
        Question question = Question.builder()
                .title(title)
                .build();

        return questionRepository.save(question);
    }

    /**
     * Обновление заголовка вопроса
     * @param id идентификатор вопроса
     * @param title заголовок вопроса
     * @return обновлённый объект типа {@link Question}
     */
    @Transactional
    public Question updateTitle(Long id, String title) {
        Question question = getById(id);
        question.setTitle(title);

        return questionRepository.save(question);
    }

    /**
     * Удаление вопроса
     * @param id идентификатор вопроса
     */
    @Transactional
    public void delete(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new EntityNotFoundException("Question not found");
        }

        questionRepository.deleteById(id);
    }
}
