-- ============================================================================
-- INITIAL SAMPLE DATA
-- Один пример вопроса и его вариантов ответов, чтобы схема не пустовала.
-- Полный набор вопросов (10 шт.) лежит в scratch_5.txt.
-- ============================================================================

-- Вопрос 1: Spring @Transactional — self-call внутри бина
INSERT INTO questions (title)
VALUES ('Что произойдёт, если метод с @Transactional вызвать через this из другого метода того же класса?');

-- Варианты ответов (ровно один правильный — is_correct = true)
INSERT INTO answers (text, question_id, is_correct, description) VALUES
    (
        'Транзакция откроется нормально — Spring перехватит вызов через прокси',
        1,
        false,
        NULL
    ),
    (
        'Транзакция НЕ откроется: this.method() минует прокси и идёт напрямую к объекту',
        1,
        true,
        'Spring оборачивает бин в прокси, и self-invocation через this ' ||
            'идёт в обход этой обёртки, поэтому @Transactional (как и @Async, ' ||
            '@Cacheable) не срабатывают. Типичная ловушка на собесах.'
    ),
    (
        'Будет RuntimeException "no active transaction" на этапе компиляции',
        1,
        false,
        NULL
    ),
    (
        'Сработает только при включённом @EnableAspectJAutoProxy(exposeProxy = true) ' ||
            'И обращении через AopContext.currentProxy()',
        1,
        false,
        NULL
    );

-- Вопрос 2: Core · equals/hashCode
INSERT INTO questions (title)
VALUES ('Что произойдёт, если переопределить equals(), но НЕ переопределить hashCode()?');

INSERT INTO answers(text, question_id, is_correct, description) VALUES
    (
        'Ничего страшного: equals достаточно, hashCode используется только в HashMap,' ||
            ' а HashMap — не единственная коллекция',
        2,
        false,
        NULL
    ),
    (
        'Объект будет корректно работать в ArrayList и TreeSet, но «потеряется» в ' ||
            'HashSet/HashMap: contains() может вернуть false для логически равного объекта',
        2,
        true,
        'Контракт требует: a.equals(b) ⇒ a.hashCode() == b.hashCode(). Обратное необязательно (коллизии ок). ' ||
            'Если equals переопределён, а hashCode — нет, используется Object.hashCode() (identity hash). ' ||
            'Объект попадает в один бакет, но equals находит совпадение только при сравнении по цепочке — ' ||
            'зависит от реализации. На практике в HashSet/HashMap это ломает поиск.'
    ),
    (
        'Компилятор выдаст предупреждение и не даст скомпилировать класс',
        2,
        false,
        NULL
    ),
    (
        'equals автоматически сгенерирует hashCode на основе всех полей',
        2,
        false,
        NULL
    );

-- Вопрос 3: Collections · HashMap сложность в худшем случае
INSERT INTO questions (title)
VALUES ('Какова сложность операции get() в HashMap в ХУДШЕМ случае (Java 8+)?');

INSERT INTO answers(text, question_id, is_correct, description) VALUES
    ('O(1) — гарантированно', 3, false, NULL),
    ('O(log n) — благодаря treeify-оптимизации при коллизиях', 3, false, NULL),
    ('O(n) — если все ключи дают одинаковый hashCode и ёмкость таблицы < 64', 3, false, NULL),
    (
        'O(log n) — но только если ёмкость таблицы >= 64',
        3,
        true,
        'TREEIFY_THRESHOLD = 8 И MIN_TREEIFY_CAPACITY = 64. Если выполнены оба условия — связный список перестраивается в red-black tree (O(log n)). ' ||
            'Если ёмкость < 64, сначала идёт resize и коллизии остаются в виде списка (O(n)). ' ||
            'Амортизированная сложность — O(1), худший случай — O(n).'
    );

-- Вопрос 4: Generics · PECS
INSERT INTO questions (title)
VALUES ('Дан метод: static void copy(List<? super Number> dst, List<? extends Number> src). Какой принцип применён и что он означает?');

INSERT INTO answers(text, question_id, is_correct, description) VALUES
    ('SPP — Source Produces Producer: src читаем, dst пишем, типы фиксированы', 4, false, NULL),
    (
        'PECS — Producer Extends, Consumer Super: src (extends) только читаем, dst (super) только пишем',
        4,
        true,
        'Producer Extends, Consumer Super. extends — можно безопасно ЧИТАТЬ (ковариантность), но НЕЛЬЗЯ добавлять. ' ||
            'super — можно безопасно ДОБАВЛЯТЬ (контрвариантность), читать только как Object.'
    ),
    ('CEPS — Consumer Extends, Producer Super: инвертированный принцип для обратной совместимости', 4, false, NULL),
    ('F-bounded — типы ограничены через рекурсивное self-ограничение', 4, false, NULL);

-- Вопрос 5: Stream API · ленивость
INSERT INTO questions (title)
VALUES ('Что выведет код: List.of(1,2,3,4,5).stream().peek(A).filter(>2).peek(B).toList();?');

INSERT INTO answers(text, question_id, is_correct, description) VALUES
    ('A1 A2 A3 B3 A4 B4 A5 B5', 5, false, NULL),
    ('A1 A2 B3 A3 A4 B4 A5 B5', 5, false, NULL),
    (
        'A1 A2 A3 B3 A4 B4 A5 B5 — элементы проходят цепочку вертикально',
        5,
        true,
        'Стрим обрабатывается вертикально — каждый элемент проходит ВСЮ цепочку перед переходом к следующему. ' ||
            'peek до filter срабатывает для всех элементов (A1..A5), peek после filter — только для прошедших (B3, B4, B5). ' ||
            'Поэтому 1 и 2 печатаются один раз, 3/4/5 — дважды.'
    ),
    ('Сначала все A для всех элементов, потом B для прошедших filter', 5, false, NULL);

-- Вопрос 6: JVM · области памяти
INSERT INTO questions (title)
VALUES ('Где в JVM хранится локальная переменная примитивного типа (например, int x = 5) внутри метода?');

INSERT INTO answers(text, question_id, is_correct, description) VALUES
    ('В heap, как и все данные в Java', 6, false, NULL),
    (
        'В stack — в фрейме текущего метода',
        6,
        true,
        'Stack содержит фреймы методов. Каждый фрейм — локальные переменные (примитивы и ссылки), операнды для вычислений. ' ||
            'Объекты (включая обёртки Integer, Long) живут в heap. PC Register хранит адрес текущей инструкции, metaspace — метаданные классов.'
    ),
    ('В metaspace — рядом с метаданными класса', 6, false, NULL),
    ('В PC Register — для быстрого доступа JIT', 6, false, NULL);

-- Вопрос 7: Multithreading · volatile
INSERT INTO questions (title)
VALUES ('Что гарантирует volatile для переменной в многопоточном коде?');

INSERT INTO answers(text, question_id, is_correct, description) VALUES
    ('Атомарность операций read/write и видимость между потоками', 7, false, NULL),
    (
        'Только видимость: запись одного потока видна другим + запрет reordering, НЕ гарантирует атомарность составных операций (i++)',
        7,
        true,
        'volatile запрещает кэширование значения в регистрах/L1 и запрещает reordering инструкций вокруг себя. ' ||
            'Но i++ — это read-modify-write (3 операции), volatile не делает это атомарным. ' ||
            'Для атомарных операций — AtomicInteger (CAS). Для составной логики — synchronized или Lock.'
    ),
    ('Только атомарность, видимость не гарантируется — для видимости нужен synchronized', 7, false, NULL),
    ('Полную потокобезопасность: можно использовать вместо synchronized везде', 7, false, NULL);

-- Вопрос 8: Multithreading · ConcurrentHashMap и null
INSERT INTO questions (title)
VALUES ('Можно ли использовать null в качестве ключа или значения в ConcurrentHashMap?');

INSERT INTO answers(text, question_id, is_correct, description) VALUES
    ('Да — null ключ и null значение допустимы, как в обычном HashMap', 8, false, NULL),
    ('Только null значение допустимо, null ключ — нет', 8, false, NULL),
    (
        'Нет — и null ключ, и null значение ЗАПРЕЩЕНЫ (бросают NullPointerException)',
        8,
        true,
        'ConcurrentHashMap запрещает null и в ключах, и в значениях. Причина — при get(key) нельзя отличить ' ||
            '«ключ отсутствует» от «ключ есть, но значение null». В многопоточной среде это сделало бы containsKey/get амбивалентными. ' ||
            'В обычном HashMap null-ключ разрешён (один).'
    ),
    ('Зависит от Java-версии: в Java 7 нельзя, в Java 8+ можно', 8, false, NULL);

-- Вопрос 9: Spring · @Component vs @Bean
INSERT INTO questions (title)
VALUES ('Чем отличаются @Component и @Bean для регистрации бина в контексте Spring?');

INSERT INTO answers(text, question_id, is_correct, description) VALUES
    ('Это синонимы, можно использовать взаимозаменяемо', 9, false, NULL),
    (
        '@Component ставится НА КЛАСС — Spring сам создаёт экземпляр через classpath scanning; @Bean ставится НА МЕТОД @Configuration-класса — метод вручную возвращает готовый объект',
        9,
        true,
        '@Component (и его наследники @Service, @Repository, @Controller) — класс-уровневая аннотация, обнаруживается @ComponentScan. ' ||
            'Spring сам инстанцирует через конструктор по умолчанию. @Bean — метод-уровневая, позволяет программно сконструировать объект ' ||
            '(с аргументами, логикой), используется для интеграции со сторонними библиотеками, где класс править нельзя.'
    ),
    ('@Component создаёт singleton, @Bean — prototype', 9, false, NULL),
    ('@Bean работает только в Spring Boot, @Component — в классическом Spring', 9, false, NULL);

-- Вопрос 10: SQL · ACID · Durability
INSERT INTO questions (title)
VALUES ('Что означает «Durability» (D в ACID) в контексте PostgreSQL и каким механизмом она обеспечивается?');

INSERT INTO answers(text, question_id, is_correct, description) VALUES
    ('Данные автоматически реплицируются на 3 узла перед COMMIT', 10, false, NULL),
    (
        'После успешного COMMIT данные переживут сбой (в том числе питания) — обеспечивается через WAL (Write-Ahead Log): сначала запись в журнал, потом в файлы данных',
        10,
        true,
        'Durability — после COMMIT данные гарантированно сохранены. PostgreSQL использует WAL: изменения сначала пишутся в журнал (fsync), ' ||
            'потом — в data files. При крахе запись восстанавливается из WAL при старте. ' ||
            'Репликация — это durability в расширенном смысле, но не часть базового механизма ACID.'
    ),
    ('Транзакция выполняется строго последовательно, без параллельных операций', 10, false, NULL),
    ('Все индексы автоматически перестраиваются после каждой записи', 10, false, NULL);