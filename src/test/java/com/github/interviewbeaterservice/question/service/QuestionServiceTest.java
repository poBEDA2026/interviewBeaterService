package com.github.interviewbeaterservice.question.service;

import com.github.interviewbeaterservice.question.entity.Category;
import com.github.interviewbeaterservice.question.entity.Difficulty;
import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.question.repository.CategoryRepository;
import com.github.interviewbeaterservice.question.repository.DifficultyRepository;
import com.github.interviewbeaterservice.question.repository.QuestionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Юнит-тесты {@link QuestionService}: сценарии метода create —
 * создание вопроса с резолвом категории и сложности из справочников по имени.
 */
@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock QuestionRepository questionRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock DifficultyRepository difficultyRepository;

    @InjectMocks QuestionService questionService;

    @Test
    @DisplayName("create резолвит категорию и сложность по имени и сохраняет вопрос со ссылками на них")
    void create_resolvesCategoryAndDifficultyByName_andSavesQuestion() {
        Category category = Category.builder().name("Core Java").build();
        Difficulty difficulty = Difficulty.builder().name("middle").level(2).build();

        when(categoryRepository.findByName("Core Java")).thenReturn(Optional.of(category));
        when(difficultyRepository.findByName("middle")).thenReturn(Optional.of(difficulty));
        when(questionRepository.save(any(Question.class))).thenAnswer(inv -> inv.getArgument(0));

        Question created = questionService.create("Что такое JPA?", "Core Java", "middle");

        assertThat(created.getTitle()).isEqualTo("Что такое JPA?");
        assertThat(created.getCategory()).isSameAs(category);
        assertThat(created.getDifficulty()).isSameAs(difficulty);
    }

    @Test
    @DisplayName("create бросает EntityNotFoundException, если категории с таким именем нет в справочнике")
    void create_throws_whenCategoryUnknown() {
        when(categoryRepository.findByName("NoSuchCategory")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.create("Q", "NoSuchCategory", "middle"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("NoSuchCategory");

        verify(questionRepository, never()).save(any());
    }

    @Test
    @DisplayName("create бросает EntityNotFoundException, если сложности с таким именем нет в справочнике")
    void create_throws_whenDifficultyUnknown() {
        when(categoryRepository.findByName("Core Java"))
                .thenReturn(Optional.of(Category.builder().name("Core Java").build()));
        when(difficultyRepository.findByName("guru")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.create("Q", "Core Java", "guru"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("guru");

        verify(questionRepository, never()).save(any());
    }
}
