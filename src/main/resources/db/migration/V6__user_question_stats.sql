-- ============================================================================
-- USER QUESTION STATS
-- Агрегированные счётчики по паре (user_id, question_id):
-- сколько раз пользователь ответил правильно/неправильно на конкретный вопрос.
-- ============================================================================

CREATE TABLE user_question_stats (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    question_id         BIGINT NOT NULL,
    correct_count       BIGINT NOT NULL DEFAULT 0,
    wrong_count         BIGINT NOT NULL DEFAULT 0,
    last_answered_at    TIMESTAMP NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_uqs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_uqs_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT uq_uqs_user_question UNIQUE (user_id, question_id)
);
