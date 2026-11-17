package com.github.interviewbeaterservice.question.service;

import com.github.interviewbeaterservice.config.CacheConfig;
import com.github.interviewbeaterservice.question.dto.QuestionResponse;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Проверяет аннотации кэширования на {@link QuestionService}. Настоящий Redis не нужен:
 * важна аннотация, а её эффект даёт CacheInterceptor поверх ConcurrentMapCacheManager.
 * Тест поднимает обычный Spring-контекст, потому что Stack-локальный ProxyFactory с
 * CacheInterceptor поверх CGLIB без afterPropertiesSet и без BeanFactoryCacheOperationSourceAdvisor
 * не подхватывает операции над методами.
 */
class QuestionServiceCacheTest {

    @Test
    void getByIdCachesById() throws Exception {
        QuestionRepository repository = mock(QuestionRepository.class);
        Question q = questionWithId(7L, "Что такое DI?");
        when(repository.findById(7L)).thenReturn(Optional.of(q));

        try (AnnotationConfigApplicationContext context = contextWith(repository)) {
            QuestionService service = context.getBean(QuestionService.class);

            QuestionResponse first = service.getById(7L);
            QuestionResponse second = service.getById(7L); // должен отдать кэш, не вызывать репозиторий повторно

            assertThat(first).isSameAs(second);
            verify(repository, times(1)).findById(7L);
            Cache questions = context.getBean(ConcurrentMapCacheManager.class).getCache(CacheConfig.QUESTIONS_CACHE);
            assertThat(questions).isNotNull();
            // В кэш кладётся DTO, а не entity: тип значения обязан совпадать с типом в CacheConfig.
            assertThat(questions.get(7L).get()).isEqualTo(QuestionResponse.from(q));   // <questions::7> сохранён
        }
    }

    /** Отсутствующий вопрос пробрасывает исключение, а не кэширует «пусто». */
    @Test
    void getByIdDoesNotCacheMissingQuestion() throws Exception {
        QuestionRepository repository = mock(QuestionRepository.class);
        when(repository.findById(99L)).thenReturn(Optional.empty());

        try (AnnotationConfigApplicationContext context = contextWith(repository)) {
            QuestionService service = context.getBean(QuestionService.class);

            assertThatThrownBy(() -> service.getById(99L)).isInstanceOf(EntityNotFoundException.class);

            verify(repository, times(1)).findById(99L);
            Cache questions = context.getBean(ConcurrentMapCacheManager.class).getCache(CacheConfig.QUESTIONS_CACHE);
            assertThat(questions.get(99L)).isNull();           // <questions::99> НЕ сохранён
        }
    }

    @Test
    void updateTitleEvicts() throws Exception {
        QuestionRepository repository = mock(QuestionRepository.class);
        Question existing = questionWithId(7L, "старое");
        when(repository.findById(7L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        try (AnnotationConfigApplicationContext context = contextWith(repository)) {
            QuestionService service = context.getBean(QuestionService.class);
            Cache questions = context.getBean(ConcurrentMapCacheManager.class).getCache(CacheConfig.QUESTIONS_CACHE);

            // прогрев кэша
            service.getById(7L);
            assertThat(questions.get(7L)).isNotNull();

            service.updateTitle(7L, "новое");

            assertThat(questions.get(7L)).isNull(); // @CacheEvict вычистил запись
            // Следующий внешний вызов обязан перечитать вопрос из БД (кэша больше нет).
            service.getById(7L);
            // прогрев + чтение вопроса внутри updateTitle + повторный внешний вызов после вычистки
            verify(repository, times(3)).findById(7L);
        }
    }

    @Test
    void deleteEvicts() throws Exception {
        QuestionRepository repository = mock(QuestionRepository.class);
        Question existing = questionWithId(7L, "любое");
        when(repository.existsById(7L)).thenReturn(true);

        try (AnnotationConfigApplicationContext context = contextWith(repository)) {
            QuestionService service = context.getBean(QuestionService.class);
            Cache questions = context.getBean(ConcurrentMapCacheManager.class).getCache(CacheConfig.QUESTIONS_CACHE);

            // положим что-то в кэш, чтобы было что вычищать
            questions.put(7L, QuestionResponse.from(existing));
            assertThat(questions.get(7L)).isNotNull();

            service.delete(7L);

            assertThat(questions.get(7L)).isNull();
            verify(repository, times(1)).deleteById(7L);
        }
    }

    private static AnnotationConfigApplicationContext contextWith(QuestionRepository repository) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.registerBean(QuestionRepository.class, () -> repository);
        context.register(TestConfig.class);
        context.refresh();
        return context;
    }

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        ConcurrentMapCacheManager cacheManager() {
            return new ConcurrentMapCacheManager(CacheConfig.QUESTIONS_CACHE);
        }

        @Bean
        QuestionService questionService(QuestionRepository repository) {
            return new QuestionService(repository);
        }
    }

    private static Question questionWithId(long id, String title) {
        Question q = Question.builder().title(title).build();
        try {
            Field f = Question.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(q, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return q;
    }
}