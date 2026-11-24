package com.github.interviewbeaterservice.answer.service;

import com.github.interviewbeaterservice.answer.dto.AnswerOption;
import com.github.interviewbeaterservice.answer.entity.Answer;
import com.github.interviewbeaterservice.answer.repository.AnswerRepository;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.service.QuestionService;
import com.github.interviewbeaterservice.statistics.service.StatisticsService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link AnswerService}: сценарии метода listOptions —
 * получение вариантов ответа на вопрос без утечки признака правильности.
 */
@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock AnswerRepository answerRepository;
    @Mock QuestionService questionService;
    @Mock StatisticsService statisticsService;

    @InjectMocks AnswerService answerService;

    /**
     * Проставить идентификатор в сущность Answer через рефлексию:
     * поле id помечено @Setter(AccessLevel.NONE), Lombok-сеттера для него нет.
     *
     * @param a  сущность ответа
     * @param id значение идентификатора
     */
    private static void setAnswerId(Answer a, Long id) {
        try {
            Field f = Answer.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(a, id);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * listOptions должен вернуть все варианты вопроса в порядке репозитория
     * и только с полями id и text: признак правильности наружу не отдаётся,
     * иначе ответ на вопрос можно подсмотреть в devtools.
     */
    @Test
    @DisplayName("listOptions отдаёт варианты только с id и text, без признака правильности")
    void listOptions_returnsIdAndTextOnly() {
        Answer correct = Answer.builder().text("Да").isCorrect(true).build();
        Answer wrong = Answer.builder().text("Нет").isCorrect(false).build();
        setAnswerId(correct, 10L);
        setAnswerId(wrong, 11L);

        when(questionService.getById(5L)).thenReturn(Question.builder().title("Тест?").build());
        when(answerRepository.findAllByQuestionId(5L)).thenReturn(List.of(correct, wrong));

        List<AnswerOption> options = answerService.listOptions(5L);

        assertThat(options).containsExactly(
                new AnswerOption(10L, "Да"),
                new AnswerOption(11L, "Нет"));
    }

    /**
     * listOptions при несуществующем вопросе должен пробросить EntityNotFoundException,
     * не опрашивая репозиторий: проверка существования вопроса выполняется до выборки ответов.
     */
    @Test
    @DisplayName("listOptions пробрасывает ошибку, если вопрос не найден")
    void listOptions_propagatesMissingQuestion() {
        when(questionService.getById(5L)).thenThrow(new EntityNotFoundException("Question 5 not found"));

        assertThatThrownBy(() -> answerService.listOptions(5L))
                .isInstanceOf(EntityNotFoundException.class);
        verify(answerRepository, never()).findAllByQuestionId(any());
    }
}
