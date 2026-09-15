-- ============================================================================
-- Question attachments stored in S3-compatible storage (MinIO locally).
-- Only metadata + storage_key live here; object bodies live in the bucket.
-- Cascade on question delete matches the existing answers pattern.
-- ============================================================================

CREATE TABLE attachments (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    question_id     BIGINT NOT NULL,
    storage_key     VARCHAR(512) NOT NULL,
    original_name   VARCHAR(255) NOT NULL,
    content_type    VARCHAR(127) NOT NULL,
    size_bytes      BIGINT NOT NULL,
    uploaded_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_attachments_question
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    CONSTRAINT uq_attachments_storage_key UNIQUE (storage_key)
);

CREATE INDEX ix_attachments_question_id ON attachments(question_id);
