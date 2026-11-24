-- ============================================================================
-- СПРАВОЧНИКИ КАТЕГОРИЙ И СЛОЖНОСТИ ВОПРОСОВ
--
-- Категория переезжает из VARCHAR-колонки questions.category (V5) в отдельную
-- таблицу-справочник, сложность — новая таблица-справочник (junior, middle,
-- middle+, senior). Обе связываются с questions через FK.
--
-- Сложность проставлена по формулировке вопроса и глубине правильного ответа:
--   junior  — базовые определения, терминология, простые факты;
--   middle  — стандартные темы: понимание механизмов и типовые ловушки;
--   middle+ — тонкие механики, внутренности реализаций, нестандартные ловушки;
--   senior  — архитектурные trade-offs, распределённые системы, диагностика.
--
-- Номер V7, а не V6: версия V6 занята несмерженной веткой user-page-front
-- (V6__add_user_full_name.sql).
-- ============================================================================

-- Справочник категорий (разделы плана подготовки)
CREATE TABLE categories (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(50) NOT NULL,
    CONSTRAINT uq_categories_name UNIQUE (name)
);

INSERT INTO categories (name) VALUES
    ('Core Java'),
    ('Spring'),
    ('Data & JPA'),
    ('JVM'),
    ('Multithreading'),
    ('Testing'),
    ('Architecture'),
    ('Databases & SQL');

-- Справочник сложности (грейды); level задаёт порядок возрастания
CREATE TABLE difficulties (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name            VARCHAR(20) NOT NULL,
    level           INT NOT NULL,
    CONSTRAINT uq_difficulties_name UNIQUE (name),
    CONSTRAINT uq_difficulties_level UNIQUE (level)
);

INSERT INTO difficulties (name, level) VALUES
    ('junior', 1),
    ('middle', 2),
    ('middle+', 3),
    ('senior', 4);

-- Ссылки из вопросов
ALTER TABLE questions ADD COLUMN category_id BIGINT;
ALTER TABLE questions ADD COLUMN difficulty_id BIGINT;

-- Перенос существующих категорий (коррелированный подзапрос — без PG-специфики UPDATE ... FROM)
UPDATE questions SET category_id = (SELECT id FROM categories WHERE name = questions.category);

-- ============================================================================
-- Сложность вопросов (id 1-10 из V2, id 11-252 из V5)
-- Покрытие: 46 junior + 122 middle + 80 middle+ + 4 senior = 252.
-- Если какой-то id пропущен во всех списках, следующий ALTER ... SET NOT NULL упадёт.
-- ============================================================================

-- junior: базовые определения и факты
UPDATE questions SET difficulty_id = (SELECT id FROM difficulties WHERE name = 'junior')
WHERE id IN (
    2, 6, 9, 11, 12, 16, 17, 19, 20, 25, 28, 29, 31, 32, 34, 38, 39, 40, 45, 46, 67, 73, 74,
    77, 78, 94, 102, 108, 111, 112, 113, 115, 117, 134, 138, 154, 155, 164, 167, 195, 196,
    229, 230, 232, 242, 250
);

-- middle: стандартные темы и типовые ловушки
UPDATE questions SET difficulty_id = (SELECT id FROM difficulties WHERE name = 'middle')
WHERE id IN (
    1, 4, 5, 7, 8, 14, 15, 18, 21, 22, 23, 26, 27, 33, 35, 36, 37, 41, 43, 44, 48, 49, 53,
    54, 55, 57, 63, 64, 65, 66, 68, 75, 76, 79, 80, 81, 82, 83, 86, 93, 95, 96, 97, 98, 99,
    100, 101, 103, 104, 105, 106, 109, 110, 114, 116, 118, 119, 121, 122, 123, 124, 125, 126,
    127, 128, 130, 133, 135, 139, 140, 143, 146, 147, 156, 157, 158, 159, 161, 162, 163, 169,
    172, 176, 180, 181, 182, 183, 184, 185, 186, 188, 189, 190, 191, 192, 193, 194, 197, 198,
    199, 200, 202, 203, 204, 205, 206, 207, 209, 210, 213, 214, 223, 224, 225, 226, 231, 235,
    239, 240, 248, 251, 252
);

-- middle+: тонкие механики, внутренности реализаций, редкие ловушки
UPDATE questions SET difficulty_id = (SELECT id FROM difficulties WHERE name = 'middle+')
WHERE id IN (
    3, 10, 13, 24, 30, 42, 47, 50, 51, 52, 56, 58, 59, 60, 61, 62, 69, 70, 71, 72, 84, 85,
    87, 88, 89, 90, 91, 92, 107, 120, 129, 131, 132, 136, 137, 141, 142, 144, 145, 148, 149,
    150, 151, 152, 153, 160, 165, 166, 168, 170, 171, 173, 175, 177, 178, 179, 187, 201, 208,
    215, 216, 217, 218, 219, 220, 221, 222, 227, 228, 233, 234, 236, 237, 238, 241, 243, 244,
    246, 247, 249
);

-- senior: архитектурные trade-offs, распределённые системы, диагностика
UPDATE questions SET difficulty_id = (SELECT id FROM difficulties WHERE name = 'senior')
WHERE id IN (
    174, 211, 212, 245
);

-- Оба поля обязательны: если выше что-то пропущено — здесь миграция упадёт
ALTER TABLE questions ALTER COLUMN category_id SET NOT NULL;
ALTER TABLE questions ALTER COLUMN difficulty_id SET NOT NULL;

ALTER TABLE questions ADD CONSTRAINT fk_questions_category
    FOREIGN KEY (category_id) REFERENCES categories(id);
ALTER TABLE questions ADD CONSTRAINT fk_questions_difficulty
    FOREIGN KEY (difficulty_id) REFERENCES difficulties(id);

-- Старую VARCHAR-колонку заменяет ссылка на справочник
ALTER TABLE questions DROP COLUMN category;
