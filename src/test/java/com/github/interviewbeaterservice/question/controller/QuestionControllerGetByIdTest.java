package com.github.interviewbeaterservice.question.controller;

import com.github.interviewbeaterservice.question.dto.QuestionResponse;
import com.github.interviewbeaterservice.question.service.QuestionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuestionControllerGetByIdTest {

    @Mock QuestionService questionService;
    @InjectMocks QuestionController controller;

    @Test
    void getByIdReturnsQuestionResponseNotEntity() throws Exception {
        QuestionResponse response = new QuestionResponse(
                42L, "Что такое ACID?", Instant.parse("2026-05-06T07:08:09Z"));

        when(questionService.getById(42L)).thenReturn(response);

        Object body = controller.getById(42L);

        assertThat(body).isInstanceOf(QuestionResponse.class);
        assertThat(body).isEqualTo(response);
    }
}