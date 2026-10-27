package com.github.interviewbeaterservice.statistics.repository;

import com.github.interviewbeaterservice.question.entity.Question;
import com.github.interviewbeaterservice.statistics.entity.UserQuestionStats;
import com.github.interviewbeaterservice.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class UserQuestionStatsRepositoryTest {

    @Autowired TestEntityManager tem;
    @Autowired UserQuestionStatsRepository repository;

    private User user;
    private Question question;

    @BeforeEach
    void setUp() {
        user = tem.persistAndFlush(User.builder().email("u@t.local").password("hash").build());
        question = tem.persistAndFlush(Question.builder().title("Lazy question").build());
        tem.persistAndFlush(UserQuestionStats.builder()
                .user(user)
                .question(question)
                .correctCount(1L)
                .wrongCount(0L)
                .lastAnsweredAt(java.time.Instant.now())
                .build());
        tem.clear();
    }

    @Test
    @DisplayName("findAllByUserId возвращает записи с инициализированным question (маппинг вне транзакции)")
    void findAllByUserId_returnsStatsWithInitializedQuestion() {
        List<UserQuestionStats> stats = repository.findAllByUserId(user.getId());

        // В проде (open-in-view=false) сессия закрывается после сервисного метода:
        // clear() отсоединяет сущности так же, как выход из транзакции, —
        // контроллер читает getTitle() уже у отсоединённого объекта.
        tem.clear();

        assertThat(stats).hasSize(1);
        assertThat(stats.get(0).getQuestion().getTitle()).isEqualTo("Lazy question");
    }

    @Test
    @DisplayName("findByUserIdAndQuestionId возвращает запись с инициализированным question")
    void findByUserIdAndQuestionId_returnsStatsWithInitializedQuestion() {
        Optional<UserQuestionStats> stats = repository.findByUserIdAndQuestionId(user.getId(), question.getId());

        tem.clear();

        assertThat(stats).isPresent();
        assertThat(stats.get().getQuestion().getTitle()).isEqualTo("Lazy question");
    }
}
