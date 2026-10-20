package com.github.interviewbeaterservice.question.service;

import com.github.interviewbeaterservice.config.CacheConfig;
import com.github.interviewbeaterservice.question.dto.QuestionResponse;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;

    /**
     * Получение вопроса по идентификатору
     * <p>
     * Возвращается плоский DTO, а не entity: {@link Question} тянет за собой ленивую связь
     * с вложениями, которую нельзя класть в Redis-кэш. Тип значения DTO обязан совпадать
     * с типом, заданным в кэше {@code questions}, — иначе запись не прочитается обратно
     *
     * @param id идентификатор вопроса
     * @return вопрос в виде DTO
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.QUESTIONS_CACHE, key = "#id")
    public QuestionResponse getById(Long id) {
        return questionRepository.findById(id)
                .map(QuestionResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Question %s not found".formatted(id)));
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
    @CacheEvict(cacheNames = CacheConfig.QUESTIONS_CACHE, key = "#id")
    public Question updateTitle(Long id, String title) {
        Question question = questionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Question %s not found".formatted(id)));
        question.setTitle(title);

        return questionRepository.save(question);
    }

    /**
     * Удаление вопроса
     * @param id идентификатор вопроса
     */
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.QUESTIONS_CACHE, key = "#id")
    public void delete(Long id) {
        if (!questionRepository.existsById(id)) {
            throw new EntityNotFoundException("Question not found");
        }

        questionRepository.deleteById(id);
    }
}
