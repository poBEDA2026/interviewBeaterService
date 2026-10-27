-- ============================================================================
-- REVIEW FEATURE: sets, items, attempts, answers
-- Бэкенд-фича "review": пользователь проходит подборки вопросов,
-- получает оценку, видит дельту с прошлой попыткой.
-- ============================================================================

CREATE TABLE review_sets (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    title           VARCHAR(255) NOT NULL,
    description     TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE review_set_items (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    set_id          BIGINT NOT NULL,
    question_id     BIGINT NOT NULL,
    position        INT NOT NULL,
    CONSTRAINT fk_rsi_set FOREIGN KEY (set_id) REFERENCES review_sets(id) ON DELETE CASCADE,
    CONSTRAINT fk_rsi_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT uq_rsi_set_position UNIQUE (set_id, position),
    CONSTRAINT uq_rsi_set_question UNIQUE (set_id, question_id)
);

CREATE INDEX idx_rsi_set_position ON review_set_items (set_id, position);

CREATE TABLE review_attempts (
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id             BIGINT NOT NULL,
    set_id              BIGINT NOT NULL,
    status              VARCHAR(20) NOT NULL,
    started_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    submitted_at        TIMESTAMP,
    total_questions     INT NOT NULL,
    correct_count       INT,
    CONSTRAINT fk_ra_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ra_set FOREIGN KEY (set_id) REFERENCES review_sets(id) ON DELETE CASCADE
);

CREATE INDEX idx_ra_user_set_status ON review_attempts (user_id, set_id, status);
CREATE INDEX idx_ra_user_set_submitted ON review_attempts (user_id, set_id, submitted_at);

CREATE TABLE review_answers (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    attempt_id      BIGINT NOT NULL,
    question_id     BIGINT NOT NULL,
    answer_id       BIGINT NOT NULL,
    is_correct      BOOLEAN,
    CONSTRAINT fk_ransw_attempt FOREIGN KEY (attempt_id) REFERENCES review_attempts(id) ON DELETE CASCADE,
    CONSTRAINT fk_ransw_question FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT fk_ransw_answer FOREIGN KEY (answer_id) REFERENCES answers(id),
    CONSTRAINT uq_ransw_attempt_question UNIQUE (attempt_id, question_id)
);
