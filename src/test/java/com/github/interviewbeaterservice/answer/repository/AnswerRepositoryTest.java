package com.github.interviewbeaterservice.answer.repository;

import com.github.interviewbeaterservice.answer.entity.Answer;
import com.github.interviewbeaterservice.question.entity.Question;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class AnswerRepositoryTest {

    @Autowired TestEntityManager tem;
    @Autowired AnswerRepository answerRepository;

    private Question q1;
    private Question q2;

    @BeforeEach
    void setUp() {
        q1 = tem.persistAndFlush(Question.builder().title("Q1").build());
        q2 = tem.persistAndFlush(Question.builder().title("Q2").build());
    }

    private Answer answerFor(Question q, String text, boolean correct) {
        Answer a = Answer.builder().text(text).isCorrect(correct).build();
        a.setQuestion(q);
        return a;
    }

    @Test
    @DisplayName("save сохраняет Answer, проставляет id и все поля")
    void save_assignsIdAndPersistsAllFields() {
        Answer a = answerFor(q1, "Some answer", true);
        a.setDescription("explanation");

        Answer saved = answerRepository.save(a);

        tem.flush();
        tem.clear();

        Answer loaded = answerRepository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getText()).isEqualTo("Some answer");
        assertThat(loaded.isCorrect()).isTrue();
        assertThat(loaded.getDescription()).isEqualTo("explanation");
        assertThat(loaded.getQuestion().getId()).isEqualTo(q1.getId());
    }

    @Test
    @DisplayName("findById возвращает ранее сохранённый Answer")
    void findById_returnsPersistedAnswer() {
        Answer saved = answerRepository.save(answerFor(q1, "A", false));

        assertThat(answerRepository.findById(saved.getId())).isPresent();
    }

    @Test
    @DisplayName("findAll возвращает все сохранённые Answer'ы")
    void findAll_returnsAllSaved() {
        answerRepository.save(answerFor(q1, "A1", false));
        answerRepository.save(answerFor(q1, "A2", true));
        answerRepository.save(answerFor(q2, "B1", false));

        assertThat(answerRepository.findAll()).hasSize(3);
    }

    @Test
    @DisplayName("deleteById удаляет Answer")
    void deleteById_removesAnswer() {
        Answer saved = answerRepository.save(answerFor(q1, "A", false));
        Long id = saved.getId();

        answerRepository.deleteById(id);
        tem.flush();

        assertThat(answerRepository.findById(id)).isEmpty();
        assertThat(answerRepository.existsById(id)).isFalse();
    }

    @Test
    @DisplayName("existsById: true после save, false после delete")
    void existsById_flipsAfterDelete() {
        Answer saved = answerRepository.save(answerFor(q1, "A", false));
        assertThat(answerRepository.existsById(saved.getId())).isTrue();

        answerRepository.deleteById(saved.getId());
        tem.flush();
        assertThat(answerRepository.existsById(saved.getId())).isFalse();
    }

    @Test
    @DisplayName("findAllByQuestionId возвращает только ответы нужного вопроса")
    void findAllByQuestionId_returnsOnlyAnswersForThatQuestion() {
        answerRepository.save(answerFor(q1, "A1", false));
        answerRepository.save(answerFor(q1, "A2", true));
        answerRepository.save(answerFor(q2, "B1", false));

        List<Answer> q1Answers = answerRepository.findAllByQuestionId(q1.getId());
        List<Answer> q2Answers = answerRepository.findAllByQuestionId(q2.getId());

        assertThat(q1Answers).hasSize(2);
        assertThat(q1Answers).allMatch(a -> a.getQuestion().getId().equals(q1.getId()));
        assertThat(q2Answers).hasSize(1);
        assertThat(q2Answers.get(0).getQuestion().getId()).isEqualTo(q2.getId());
    }

    @Test
    @DisplayName("findAllByQuestionId для вопроса без ответов возвращает пустой список")
    void findAllByQuestionId_emptyList_whenNoAnswers() {
        Question empty = tem.persistAndFlush(Question.builder().title("Empty").build());

        List<Answer> result = answerRepository.findAllByQuestionId(empty.getId());

        assertThat(result).isNotNull().isEmpty();
    }
}