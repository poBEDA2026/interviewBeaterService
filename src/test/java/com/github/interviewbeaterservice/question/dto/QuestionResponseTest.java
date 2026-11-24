package com.github.interviewbeaterservice.question.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.interviewbeaterservice.question.entity.Question;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionResponseTest {

    @Test
    void fromCopiesCoreFieldsAndIgnoresAttachments() throws Exception {
        Question q = Question.builder().title("Что такое Spring Cache?").build();
        setField(q, "id", 7L);
        setField(q, "createdAt", Instant.parse("2026-01-02T03:04:05Z"));

        QuestionResponse response = QuestionResponse.from(q);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.title()).isEqualTo("Что такое Spring Cache?");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-01-02T03:04:05Z"));
    }

    @Test
    void jsonShapeIsIdTitleCreatedAtOnly() throws Exception {
        QuestionResponse response = new QuestionResponse(7L, "Что такое Spring Cache?",
                Instant.parse("2026-01-02T03:04:05Z"));

        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        var node = mapper.readTree(mapper.writeValueAsString(response));

        assertThat(node.fieldNames()).toIterable().containsExactlyInAnyOrder("id", "title", "createdAt");
    }

    /**
     * Question собирается через @Builder без сеттеров для id/createdAt
     * поэтому проставляем их рефлексией — так тест не зависит от сеттеров entity
     */
    private static void setField(Question question, String name, Object value) throws Exception {
        Field field = Question.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(question, value);
    }
}