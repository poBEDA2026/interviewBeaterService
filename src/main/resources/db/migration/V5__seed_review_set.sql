-- V5__seed_review_set.sql
-- Демо-подборка для прогона фичи review: 1 подборка, 3 вопроса из V2.
-- question_id 1, 2, 3 — реальные ID, сидированные в V2__initial_filling.sql.

INSERT INTO review_sets (title, description) VALUES ('Core Java — вводная', 'Базовая подборка для старта');

INSERT INTO review_set_items (set_id, question_id, position) VALUES
    (1, 1, 0),
    (1, 2, 1),
    (1, 3, 2);
