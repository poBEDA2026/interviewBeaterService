-- ============================================================================
-- QUESTIONS FROM INTERVIEW-PREP.md
-- Все вопросы из плана подготовки (все блоки, независимо от статуса изучения).
-- Нумерация продолжает V2 (там заняты id 1-10).
-- У каждого вопроса 3-4 варианта, ровно один is_correct = true.
-- ============================================================================

-- Увеличиваем лимит описания: развёрнутые пояснения не влезали в 1000 символов
ALTER TABLE answers ALTER COLUMN description TYPE VARCHAR(2000);

-- Категория вопроса (раздел плана подготовки)
ALTER TABLE questions ADD COLUMN category VARCHAR(50);

-- Категории для вопросов из V2 (id 1-10)
UPDATE questions SET category = 'Spring'            WHERE id = 1;
UPDATE questions SET category = 'Core Java'         WHERE id IN (2, 3, 4, 5);
UPDATE questions SET category = 'JVM'               WHERE id = 6;
UPDATE questions SET category = 'Multithreading'    WHERE id IN (7, 8);
UPDATE questions SET category = 'Spring'            WHERE id = 9;
UPDATE questions SET category = 'Databases & SQL'   WHERE id = 10;

-- ============================================================================
-- 1. CORE JAVA
-- ============================================================================

-- 11-15: Singleton
INSERT INTO questions (title) VALUES
    ('Зачем в Singleton private constructor?'),
    ('В чём разница между eager и lazy инициализацией Singleton?'),
    ('Зачем volatile в Double-Checked Locking (DCL)?'),
    ('Почему Enum Singleton считается самым безопасным?'),
    ('Чем Spring singleton scope отличается от GoF Singleton?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Чтобы нельзя было создать экземпляр через new — единственный экземпляр создаётся внутри самого класса', 11, true,
        'Private constructor запрещает внешнее new. Единственная точка создания — static getInstance(). Без этого инвариант «ровно один экземпляр» не гарантировать.'),
    ('Чтобы класс нельзя было унаследовать', 11, false, NULL),
    ('Это требование компилятора для static-полей', 11, false, NULL),
    ('Для потокобезопасности самого конструктора', 11, false, NULL),

    ('Eager создаётся при загрузке класса, lazy — при первом обращении', 12, true,
        'Eager — экземпляр создаётся сразу при инициализации класса (просто и потокобезопасно). Lazy — при первом вызове getInstance(), экономит ресурсы, но требует защиты от гонок (DCL, holder, enum).'),
    ('Eager — это когда конструктор приватный, lazy — публичный', 12, false, NULL),
    ('Eager работает только в многопоточном коде', 12, false, NULL),
    ('Разницы нет, это синонимы', 12, false, NULL),

    ('Чтобы гарантировать видимость частично сконструированного объекта другим потокам и запретить reordering', 13, true,
        'Без volatile один поток может увидеть ссылку, записанную ДО завершения конструктора (reordering: запись ссылки раньше записи полей). volatile гарантирует visibility и запрещает reordering записей вокруг неё.'),
    ('Чтобы сделать операцию проверки и создания атомарной', 13, false,
        'volatile НЕ делает составные операции атомарными — для этого synchronized. В DCL он отвечает за visibility и reordering.'),
    ('Это просто соглашение стиля кода', 13, false, NULL),
    ('volatile нужен только в eager-реализации', 13, false, NULL),

    ('Защищён от reflection и сериализации: JVM гарантирует единственный экземпляр', 14, true,
        'Enum создаётся JVM ровно один раз: reflection не может вызвать enum-конструктор (InstantiationException), а при десериализации JVM восстанавливает существующий экземпляр, а не создаёт новый.'),
    ('Он самый быстрый при создании', 14, false, NULL),
    ('Enum не может быть null', 14, false, NULL),
    ('Enum автоматически синхронизирует все свои методы', 14, false, NULL),

    ('Spring singleton — один бин на контейнер, GoF — один экземпляр на ClassLoader/JVM', 15, true,
        'GoF Singleton — жёсткая гарантия «один экземпляр на JVM» через приватный конструктор. Spring singleton scope — один бин НА КОНТЕКСТ:Spring может создать несколько бинов одного класса в разных контейнерах, а класс остаётся обычным.'),
    ('Это одно и то же, только разные термины', 15, false, NULL),
    ('Spring singleton — новый экземпляр на каждый запрос', 15, false, NULL),
    ('GoF Singleton работает только в Spring-приложениях', 15, false, NULL);

-- 16-20: Checked vs Unchecked
INSERT INTO questions (title) VALUES
    ('Какие из перечисленных являются checked exceptions?'),
    ('Какие из перечисленных являются unchecked exceptions?'),
    ('Когда использовать checked, а когда unchecked exceptions?'),
    ('Что произойдёт, если не обработать checked exception?'),
    ('Error — это checked или unchecked?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('IOException, SQLException, FileNotFoundException', 16, true,
        'Checked (наследуют Exception, но не RuntimeException) — внешние проблемы, которые caller может обработать: файл не найден, сеть упала, БД недоступна. Компилятор требует throws или try-catch.'),
    ('NullPointerException, IllegalArgumentException, IndexOutOfBoundsException', 16, false, NULL),
    ('OutOfMemoryError, StackOverflowError, AssertionError', 16, false, NULL),
    ('ClassCastException, ArrayStoreException, UnsupportedOperationException', 16, false, NULL),

    ('NullPointerException, IllegalArgumentException, IndexOutOfBoundsException', 17, true,
        'Unchecked (наследуют RuntimeException) — баги в коде, а не внешние проблемы: NPE, неверный аргумент, выход за границы. Обработка не обязательна.'),
    ('IOException, SQLException, ClassNotFoundException', 17, false, NULL),
    ('InterruptedException, CloneNotSupportedException, ParseException', 17, false, NULL),
    ('OutOfMemoryError, ExceptionInInitializerError, NoSuchFieldError', 17, false, NULL),

    ('Checked — для восстановимых внешних проблем (caller может отреагировать), unchecked — для багов в коде', 18, true,
        'Checked — когда caller МОЖЕТ и ДОЛЖЕН отреагировать (повторить запрос, показать сообщение). Unchecked — когда восстановление невозможно: это программная ошибка, которую нужно чинить, а не ловить.'),
    ('Checked — для частых ошибок, unchecked — для редких', 18, false, NULL),
    ('Checked — в библиотеках, unchecked — в приложениях', 18, false, NULL),
    ('Разницы нет, выбор — вопрос стиля', 18, false, NULL),

    ('Код не скомпилируется: компилятор требует throws или try-catch', 19, true,
        'Для checked exception компилятор принуждает обработку: либо catch, либо объявить throws в сигнатуре. Иначе — ошибка компиляции.'),
    ('Код скомпилируется, exception молча проигнорируется', 19, false, NULL),
    ('В runtime приложение упадёт, но только если exception случится', 19, false, NULL),
    ('JVM автоматически оборачивает его в RuntimeException', 19, false, NULL),

    ('Unchecked: наследует Throwable напрямую, обрабатывать не обязательно', 20, true,
        'Error наследует Throwable (не Exception) и является unchecked. Это фатальные проблемы JVM (OOM, StackOverflow), которые не предполагают восстановление — ловить их не следует.'),
    ('Checked: его нужно обязательно обрабатывать', 20, false, NULL),
    ('Ни то ни другое: Error — отдельная категория вне иерархии Throwable', 20, false, NULL),
    ('Зависит от конкретного подкласса Error', 20, false, NULL);

-- 21-24: volatile
INSERT INTO questions (title) VALUES
    ('Что такое visibility и почему без volatile потоки видят устаревшее значение?'),
    ('Почему volatile int i; i++ не является thread-safe?'),
    ('Когда volatile достаточно, а когда нет?'),
    ('Как volatile связан с happens-before?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Поток может кэшировать переменную в регистре/кэше ядра; volatile запрещает это и читает из основной памяти', 21, true,
        'Без volatile JIT/процессор вправе держать значение в кэше потока — изменения другого потока не видны. volatile гарантирует: запись сразу в основную память, чтение — из неё.'),
    ('Visibility — это атомарность записи ссылок; без volatile запись не атомарна', 21, false, NULL),
    ('Потоки работают с копиями объекта; без volatile копии не синхронизируются никогда', 21, false, NULL),
    ('Visibility гарантирует GC; без volatile GC не удаляет объекты вовремя', 21, false, NULL),

    ('Потому что i++ — это три операции (read, modify, write), а volatile не делает их атомарными', 22, true,
        'volatile даёт visibility и запрет reordering, но НЕ атомарность. Между чтением и записью другого потока вклинится обновление — обновление потеряется. Нужен AtomicInteger (CAS) или synchronized.'),
    ('Потому что volatile переменные нельзя инкрементировать', 22, false, NULL),
    ('Потому что i++ требует synchronized на уровне компилятора', 22, false, NULL),
    ('На самом деле i++ с volatile полностью потокобезопасен', 22, false, NULL),

    ('Достаточно для флагов и статусов (одна запись — много чтений); недостаточно для составных операций вроде counter++', 23, true,
        'volatile подходит, когда значение пишется одним потоком, а читается другими (флаг остановки, статус) и нет read-modify-write. Для счётчиков и инвариантов между несколькими переменными — synchronized/Atomic*.'),
    ('Volatile работает только с примитивами, с объектами — никогда', 23, false, NULL),
    ('Volatile достаточен везде, где не нужны транзакции БД', 23, false, NULL),
    ('Volatile никогда не достаточен, всегда нужен synchronized', 23, false, NULL),

    ('Запись в volatile устанавливает happens-before с последующим чтением этой переменной другим потоком', 24, true,
        'По JMM запись в volatile happens-before чтение той же переменной. Поэтому всё, что поток А сделал до записи, видно потоку Б после чтения — так volatile «публикует» и обычные поля тоже.'),
    ('Volatile отключает happens-before для обычных полей', 24, false, NULL),
    ('Happens-before применим только к synchronized, volatile с ним не связан', 24, false, NULL),
    ('Volatile создаёт happens-before между любыми переменными любых потоков', 24, false, NULL);

-- 25-30: Collections
INSERT INTO questions (title) VALUES
    ('В чём разница ArrayList и LinkedList?'),
    ('Как HashMap устроена внутри?'),
    ('Что произойдёт, если два ключа имеют одинаковый hashCode?'),
    ('Зачем переопределять equals и hashCode вместе?'),
    ('В чём разница HashMap и TreeMap?'),
    ('Чем AVL-дерево отличается от красно-чёрного и где какое используется?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('ArrayList — массив с O(1) доступом по индексу; LinkedList — двусвязный список с O(1) вставкой в начало/конец', 25, true,
        'ArrayList: доступ по индексу O(1), вставка в середину O(n). LinkedList: вставка/удаление в известных узлах O(1), доступ по индексу O(n). На практике ArrayList быстрее почти всегда из-за локальности кэша.'),
    ('ArrayList синхронизирован, LinkedList — нет', 25, false, NULL),
    ('ArrayList хранит примитивы, LinkedList — только объекты', 25, false, NULL),
    ('Разницы нет, выбор — вопрос стиля', 25, false, NULL),

    ('Массив бакетов; индекс = (n-1) & hash; коллизии — связный список, при >8 элементов — красно-чёрное дерево', 26, true,
        'HashMap — массив бакетов (capacity — степень двойки). Hash смешивается через h ^ (h >>> 16). Индекс бакета: (n-1) & hash. Коллизии разрешаются списком; с Java 8 при >8 элементах в бакете (и capacity >= 64) список превращается в красно-чёрное дерево — поиск O(log n).'),
    ('Дерево пар ключ-значение, отсортированное по hashCode', 26, false, NULL),
    ('Хэш-таблица с открытой адресацией и линейным пробированием', 26, false, NULL),
    ('Список записей с линейным поиском по equals', 26, false, NULL),

    ('Ключи попадут в один бакет — коллизия; внутри бакета различаются через equals', 27, true,
        'Одинаковый hashCode не запрещён: оба ключа попадут в один бакет (или в разные из-за spread-функции, но с равной вероятностью). Внутри бакета значения различаются через equals: совпал — перезапись, нет — добавление в цепочку.'),
    ('HashMap выбросит IllegalStateException', 27, false, NULL),
    ('Второй ключ молча игнорируется', 27, false, NULL),
    ('HashMap автоматически увеличит capacity в два раза', 27, false, NULL),

    ('Нарушение контракта: равные объекты должны иметь равные hashCode, иначе хэш-коллекции работают неверно', 28, true,
        'Контракт: a.equals(b) => a.hashCode() == b.hashCode(). Если переопределить только equals, равные объекты попадут в разные бакеты и HashSet/HashMap не найдут дубликат. Обратное (равный hashCode при неравных equals — коллизия) допустимо.'),
    ('Требование компилятора для наследников Object', 28, false, NULL),
    ('Чтобы equals работал быстрее', 28, false, NULL),
    ('Это нужно только для TreeMap', 28, false, NULL),

    ('HashMap — O(1) без порядка; TreeMap — красно-чёрное дерево с сортировкой ключей и O(log n)', 29, true,
        'HashMap: хэш-таблица, O(1) на операции, порядок итерации не определён. TreeMap: красно-чёрное дерево, ключи отсортированы (Comparable/Comparator), O(log n), поддерживает firstKey/floorKey и т.п.'),
    ('TreeMap быстрее на всех операциях', 29, false, NULL),
    ('HashMap допускает null-значения, TreeMap — нет; это единственная разница', 29, false, NULL),
    ('HashMap хранит ключи отсортированно, TreeMap — в порядке вставки', 29, false, NULL),

    ('AVL строже сбалансирован (быстрее поиск, больше поворотов), RBT мягче (дешевле запись) — RBT в TreeMap и бакетах HashMap', 30, true,
        'AVL: высоты поддеревьев отличаются не более чем на 1 — почти идеальный баланс, быстрый поиск, но больше поворотов при изменениях. RBT: длинный путь <= 2x короткого — меньше поворотов. AVL выгоден в read-heavy, RBT — в write-heavy (поэтому TreeMap и HashMap используют RBT).'),
    ('AVL медленнее в поиске, но быстрее во вставке', 30, false, NULL),
    ('Красно-чёрное дерево всегда ниже AVL по высоте', 30, false, NULL),
    ('Это разные названия одного и того же дерева', 30, false, NULL);

-- 31-34: DI
INSERT INTO questions (title) VALUES
    ('Что такое IoC и как DI с ним связан?'),
    ('Какие типы DI существуют и какой рекомендуется?'),
    ('Почему Field Injection не рекомендуется?'),
    ('Какие DI-фреймворки существуют кроме Spring?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('IoC — принцип «контейнер управляет созданием и связыванием объектов»; DI — конкретный способ реализации IoC', 31, true,
        'Inversion of Control — объект не создаёт свои зависимости сам, а получает их от контейнера. Dependency Injection — самая распространённая реализация IoC (другие: Service Locator, события).'),
    ('IoC и DI — синонимы, означают одно и то же', 31, false, NULL),
    ('IoC — это паттерн наследования, DI — композиции', 31, false, NULL),
    ('DI — фреймворк, IoC — библиотека внутри него', 31, false, NULL),

    ('Constructor (рекомендуется), Setter, Field', 32, true,
        'Constructor injection — рекомендуемый: неизменяемость (final), все зависимости обязательны, легко тестировать. Setter — для optional зависимостей. Field — не рекомендуется.'),
    ('Setter (рекомендуется), Constructor, Field', 32, false, NULL),
    ('Field (рекомендуется) как самый лаконичный', 32, false, NULL),
    ('Только Constructor — остальные типы запрещены в Spring', 32, false, NULL),

    ('Трудно тестировать (нужна рефлексия), скрывает зависимости, допускает циклические зависимости и mutable-состояние', 33, true,
        'Field injection: нельзя объявить поля final, для unit-тестов нужен рефлексивный вмешательство в приватные поля, класс скрывает, сколько у него зависимостей, и маскирует слишком большие классы.'),
    ('Field injection медленнее в runtime из-за прокси', 33, false, NULL),
    ('Spring не умеет внедрять зависимости в поля', 33, false, NULL),
    ('Field injection работает только с @Configuration классами', 33, false, NULL),

    ('Google Guice, Jakarta CDI (Weld), Micronaut, Dagger', 34, true,
        'Кроме Spring: Google Guice, Jakarta/JSR-330 CDI (Weld, OpenWebBeans), Micronaut (compile-time DI), Dagger (compile-time, Android), PicoContainer, Koin (Kotlin).'),
    ('Hibernate, JUnit, Mockito', 34, false, NULL),
    ('Tomcat, Jetty, Netty', 34, false, NULL),
    ('Кроме Spring DI-фреймворков не существует', 34, false, NULL);

-- 35-38: @Autowired
INSERT INTO questions (title) VALUES
    ('Когда @Autowired можно не писать?'),
    ('Что делает @Autowired(required = false)?'),
    ('Что произойдёт, если есть несколько бинов одного типа?'),
    ('Как @Autowired связан с @Qualifier?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('С Spring 4.3+ — если у класса один конструктор', 35, true,
        'Если у бина ровно один конструктор, Spring внедрит зависимости в него без аннотации. При нескольких конструкторах @Autowired нужен на том, который использовать.'),
    ('Никогда — аннотация обязательна всегда', 35, false, NULL),
    ('Только для prototype-бинов', 35, false, NULL),
    ('Только если бин объявлен через @Bean', 35, false, NULL),

    ('Делает зависимость необязательной: если бин не найден, поле останется null вместо ошибки', 36, true,
        'По умолчанию Spring падает при отсутствии кандидата. required = false разрешает отсутствие зависимости — точка внедрения останется незаполненной.'),
    ('Требует, чтобы зависимость была создана до старта контекста', 36, false, NULL),
    ('Отключает прокси для зависимого бина', 36, false, NULL),
    ('Указывает внедрять только через setter', 36, false, NULL),

    ('Spring бросит NoUniqueBeanDefinitionException, если нет @Primary или @Qualifier', 37, true,
        'При нескольких кандидатах одного типа Spring не может выбрать: падает NoUniqueBeanDefinitionException. Разрешается через @Primary (дефолтный), @Qualifier (явное имя) или имя параметра, совпадающее с именем бина.'),
    ('Spring внедрит первый найденный бин', 37, false, NULL),
    ('Spring создаст ещё один экземпляр', 37, false, NULL),
    ('Spring внедрит все бины списком автоматически', 37, false, NULL),

    ('@Qualifier уточняет, какой именно бин внедрить, когда кандидатов несколько', 38, true,
        '@Autowired находит бин по типу; @Qualifier добавляет фильтр по имени/метке, устраняя неоднозначность при нескольких бинах одного типа.'),
    ('@Qualifier заменяет @Autowired при field injection', 38, false, NULL),
    ('@Autowired работает по имени, @Qualifier — по типу', 38, false, NULL),
    ('Никак, это независимые аннотации', 38, false, NULL);

-- 39-42: WAR vs Fat JAR
INSERT INTO questions (title) VALUES
    ('В чём разница WAR и Fat JAR?'),
    ('Что такое embedded server?'),
    ('Какой подход упаковки лучше для Docker/Kubernetes?'),
    ('Как Spring Boot создаёт Fat JAR?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('WAR — для внешнего servlet-контейнера; Fat JAR — самодостаточный, с embedded-сервером, запускается java -jar', 39, true,
        'WAR разворачивается вTomcat/WebLogic/Jetty. Fat JAR содержит все зависимости и embedded-сервер внутри: java -jar app.jar — и приложение работает. Fat JAR — облачный подход.'),
    ('WAR содержит embedded-сервер, Fat JAR требует внешний Tomcat', 39, false, NULL),
    ('Это разные расширения одного формата', 39, false, NULL),
    ('Fat JAR — это WAR, переименованный в JAR', 39, false, NULL),

    ('HTTP-сервер (Tomcat/Jetty/Undertow), встроенный в само приложение как библиотека', 40, true,
        'Embedded server — часть приложения: запускается в методе main вместе с контекстом. Не нужно устанавливать и настраивать отдельный контейнер — деплой упрощается до одного артефакта.'),
    ('Отдельный Docker-контейнер с Tomcat рядом с приложением', 40, false, NULL),
    ('Сервер, который разворачивает WAR-файлы из папки webapps', 40, false, NULL),
    ('Кластерный балансировщик нагрузки перед приложением', 40, false, NULL),

    ('Fat JAR с embedded-сервером', 41, true,
        'Контейнеры любят «один артефакт — один процесс»: Fat JAR не требует внешнего контейнера, легко кэшируется слоями Docker-образа и масштабируется без конфигурации сервлет-контейнеров.'),
    ('WAR в заранее установленном Tomcat', 41, false, NULL),
    ('Разницы нет, оба подхода эквивалентны для облака', 41, false, NULL),
    ('Ни один — в облако деплоят исходники', 41, false, NULL),

    ('Через Maven/Gradle plugin: он упаковывает зависимости и загрузчик классов JarLauncher в один JAR', 42, true,
        'Spring Boot Maven/Gradle plugin репакует JAR: зависимости кладёт в BOOT-INF/lib, классы — в BOOT-INF/classes, добавляет JarLauncher, который умеет загружать классы из вложенных JAR — обычный ClassLoader этого не умеет.'),
    ('Компилятор javac автоматически включает зависимости в JAR', 42, false, NULL),
    ('Fat JAR собирается утилитой shade только в Maven', 42, false, NULL),
    ('Spring Boot кладёт WAR внутрь JAR при сборке', 42, false, NULL);

-- ============================================================================
-- 2. SPRING FRAMEWORK
-- ============================================================================

-- 43-47: @Component vs @Bean
INSERT INTO questions (title) VALUES
    ('Когда использовать @Component, а когда @Bean?'),
    ('Что делает @Repository помимо регистрации бина?'),
    ('В чём разница @Controller и @RestController?'),
    ('Что делает @Service кроме семантики?'),
    ('Что будет, если на одном классе поставить @Component и объявить для него @Bean-метод?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('@Component — для своих классов (сканирование); @Bean — для сторонних классов и сложной инициализации', 43, true,
        '@Component на своём классе — Spring сам создаёт бин через component scan. @Bean на методе @Configuration — вы сами пишете код создания: нужен для third-party классов и нестандартной инициализации.'),
    ('@Bean — для своих классов, @Component — для сторонних', 43, false, NULL),
    ('Это синонимы, можно использовать взаимозаменяемо', 43, false, NULL),
    ('@Component работает только в Spring Boot', 43, false, NULL),

    ('Переводит технологические исключения (SQLException и т.п.) в DataAccessException иерархию', 44, true,
        '@Repository = @Component + exception translation: PersistenceExceptionTranslator оборачивает vendor-специфичные исключения в согласованную иерархию DataAccessException.'),
    ('Делает бин потокобезопасным', 44, false, NULL),
    ('Включает кэширование методов доступа к данным', 44, false, NULL),
    ('Ничего: это просто алиас @Component', 44, false, NULL),

    ('@RestController = @Controller + @ResponseBody: методы возвращают JSON/XML, а не имя view', 45, true,
        '@Controller работает с view-технологиями (Thymeleaf, JSP) — возвращает имя шаблона. @RestController добавляет @ResponseBody: каждый метод возвращает данные (JSON) напрямую, без шаблонов.'),
    ('@Controller для REST, @RestController для MVC', 45, false, NULL),
    ('@RestController устарел с Spring Boot 3', 45, false, NULL),
    ('Разница только в имени, поведение идентично', 45, false, NULL),

    ('Ничего, кроме читаемости: это чисто семантический алиас @Component', 46, true,
        '@Service не добавляет поведения — только маркирует слой бизнес-логики для читаемости и (потенциально) аспектов. Вся функциональность приходит от @Component.'),
    ('Включает транзакционность всех методов класса', 46, false, NULL),
    ('Добавляет exception translation как @Repository', 46, false, NULL),
    ('Регистрирует бин в JMX как сервис', 46, false, NULL),

    ('Будут зарегистрированы два бина — конфликт по имени/типу, если не разрулить @Primary/@Qualifier', 47, true,
        '@Component зарегистрирует бин через сканирование, а @Bean-метод (в @Configuration) создаст второй бин того же типа/имени. По типу станет несколько кандидатов → нужно @Primary, @Qualifier или разные имена.'),
    ('Spring проигнорирует @Component', 47, false, NULL),
    ('Spring проигнорирует @Bean-метод', 47, false, NULL),
    ('Приложение не скомпилируется', 47, false, NULL);

-- 48-52: @Scope
INSERT INTO questions (title) VALUES
    ('Какие scope бывают в Spring?'),
    ('Почему singleton-бины должны быть stateless?'),
    ('Как внедрить prototype-бин в singleton?'),
    ('Зачем proxyMode для request/session scope?'),
    ('Что такое ScopedProxyMode.TARGET_CLASS?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('singleton (по умолчанию), prototype, request, session, application, websocket', 48, true,
        'singleton — один бин на контейнер; prototype — новый на каждый запрос; request/session/application/websocket — веб-области (нужен proxyMode или веб-контекст).'),
    ('Только singleton и prototype', 48, false, NULL),
    ('singleton, prototype, static, dynamic', 48, false, NULL),
    ('Scope бывают только в Jakarta EE, в Spring их нет', 48, false, NULL),

    ('Один экземпляр обслуживает все запросы — mutable-состояние вызовет гонки данных', 49, true,
        'Singleton-бин разделяется всеми потоками. Изменяемое поле одного запроса затирается другим. Состояние нужно держать в параметрах/локальных переменных или выносить в prototype/request-бины.'),
    ('Иначе Spring не сможет сериализовать бин', 49, false, NULL),
    ('Потому что stateless-бины быстрее создаются', 49, false, NULL),
    ('Требование GC: mutable-объекты собираются медленнее', 49, false, NULL),

    ('Через ObjectProvider<T> / Provider<T> или @Lookup-метод', 50, true,
        'Обычная инъекция выдаст один prototype-бин навсегда. ObjectProvider.get() или @Lookup выдаёт новый экземпляр при каждом обращении — делегирование создания контейнеру в момент вызова.'),
    ('Никак, prototype нельзя внедрить в singleton', 50, false, NULL),
    ('Через @Scope("prototype") на поле singleton-бина', 50, false, NULL),
    ('Через Cloneable интерфейс', 50, false, NULL),

    ('Чтобы внедрить прокси вместо реального бина: он достаёт бин нужной области при каждом вызове метода', 51, true,
        'Singleton-зависимость внедряется один раз, а request-бин живёт один HTTP-запрос. Proxy-обёртка при каждом вызове метода достаёт экземпляр текущего запроса/сессии из контекста.'),
    ('ProxyMode ускоряет создание бинов через кэш', 51, false, NULL),
    ('Он включает сериализацию бинов в HTTP-сессию', 51, false, NULL),
    ('Это способ задать custom scope программно', 51, false, NULL),

    ('Режим CGLIB: генерируется подкласс целевого бина, работающий как scoped proxy', 52, true,
        'TARGET_CLASS — CGLIB создаёт прокси-подкласс реального класса (даже без интерфейсов). INTERFACE — JDK-прокси по интерфейсам; если интерфейса нет, нужен TARGET_CLASS.'),
    ('Это прокси над интерфейсом javax.ejb.Target', 52, false, NULL),
    ('Режим, при котором прокси копирует состояние цели', 52, false, NULL),
    ('Способ отключить прокси для конкретного бина', 52, false, NULL);

-- 53-62: @Transactional
INSERT INTO questions (title) VALUES
    ('Почему рекомендуется rollbackFor = Exception.class?'),
    ('В чём разница REQUIRED и REQUIRES_NEW?'),
    ('Почему self-invocation не работает с @Transactional?'),
    ('Что произойдёт, если поймать exception внутри вложенного REQUIRED-метода?'),
    ('Для чего readOnly = true?'),
    ('Метод A (@Transactional) вызывает метод B (@Transactional REQUIRES_NEW), B бросает exception. Что с транзакцией A?'),
    ('Метод A вызывает this.methodB(), где methodB — private с @Transactional. Сработает ли транзакция для B?'),
    ('Почему @Transactional не распространяется через @Async?'),
    ('В какой момент коммитится транзакция REQUIRES_NEW?'),
    ('Как спасти основную транзакцию, если вложенная операция может упасть?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('По умолчанию rollback только для unchecked; rollbackFor = Exception.class откатывает и checked', 53, true,
        'Spring откатывает транзакцию только при RuntimeException/Error. Checked exception по умолчанию НЕ откатывает — частая ловушка. rollbackFor = Exception.class делает поведение предсказуемым: любое исключение откатывает.'),
    ('Чтобы checked exception не ломал компиляцию', 53, false, NULL),
    ('Это ускоряет работу транзакции', 53, false, NULL),
    ('Иначе Spring не сможет открыть соединение с БД', 53, false, NULL),

    ('REQUIRED присоединяется к существующей транзакции; REQUIRES_NEW всегда открывает новую независимую', 54, true,
        'REQUIRED: если транзакция есть — используем её, нет — создаём (по умолчанию). REQUIRES_NEW приостанавливает текущую и открывает новую: её commit/rollback не влияет на внешнюю.'),
    ('REQUIRED открывает новую транзакцию всегда', 54, false, NULL),
    ('REQUIRES_NEW работает только с readOnly', 54, false, NULL),
    ('Разница только в уровне изоляции', 54, false, NULL),

    ('Вызов через this идёт мимо AOP-прокси, который и открывает транзакцию', 55, true,
        'Транзакционность реализована прокси: транзакцию открывает обёртка перед вызовом метода. this.method() вызывает целевой объект напрямую, минуя прокси — аннотация игнорируется. То же с @Async, @Cacheable, @PreAuthorize.'),
    ('Private-методы не поддерживаются JVM', 55, false, NULL),
    ('Spring запрещает несколько транзакций в одном бине', 55, false, NULL),
    ('Компилятор удаляет аннотации при self-invocation', 55, false, NULL),

    ('Если exception пойман, но внутренний REQUIRED пометил транзакцию rollback-only — commit всё равно откатит её', 56, true,
        'Вложенный метод в REQUIRED работает в той же физической транзакции: при unchecked-исключении он ставит rollback-only. Поймать exception снаружи недостаточно — при коммите будет UnexpectedRollbackException. Спасение: REQUIRES_NEW + try-catch, noRollbackFor, NESTED или TransactionTemplate.'),
    ('Пойманный exception полностью отменяет откат — транзакция закоммитится', 56, false, NULL),
    ('Spring выбросит исключение компиляции', 56, false, NULL),
    ('Транзакция перезапустится автоматически с начала', 56, false, NULL),

    ('Оптимизация SELECT-методов: flush отключён, Hibernate может не делать dirty checking', 57, true,
        'readOnly = true сообщает, что транзакция только читает: драйвер/БД могут оптимизировать, Hibernate отключает dirty checking, пропадает snapshot-накладной расход.'),
    ('Запрещает любые SELECT в транзакции', 57, false, NULL),
    ('Указывает использовать read-only реплику БД', 57, false, NULL),
    ('Включает Optimistic Locking для сущностей', 57, false, NULL),

    ('Откатится только транзакция B; A продолжит работу и сможет закоммититься, если поймает exception', 58, true,
        'REQUIRES_NEW — отдельная физическая транзакция: rollback B не трогает A. Но если exception вылетит из B без catch, A будет помечена rollback-only при выходе через прокси — поэтому B оборачивают в try-catch.'),
    ('Откатятся обе транзакции всегда', 58, false, NULL),
    ('Транзакция A закоммитится частично', 58, false, NULL),
    ('A будет ждать, пока B не закоммитится вручную', 58, false, NULL),

    ('Нет: self-invocation мимо прокси, плюс Spring не транзакционирует private-методы', 59, true,
        'Двойная причина: вызов через this обходит AOP-прокси, а CGLIB-прокси вообще не переопределяет private-методы. Аннотация молча игнорируется. Решение: вынести в другой бин или вызвать через self-прокси.'),
    ('Да, транзакция откроется как обычно', 59, false, NULL),
    ('Да, но только с уровнем изоляции SERIALIZABLE', 59, false, NULL),
    ('Метод упадёт с UnsupportedOperationException', 59, false, NULL),

    ('@Async = другой поток; транзакция привязана к JDBC-connection в ThreadLocal текущего потока', 60, true,
        'Транзакция живёт на одном connection, который Spring биндит в ThreadLocal (TransactionSynchronizationManager). Новый поток из пула получает пустой ThreadLocal и своё соединение — присоединиться к транзакции родителя не к чему.'),
    ('@Async отключает @Transactional аннотацией @DisableTransaction', 60, false, NULL),
    ('Потому что транзакции работают только в main-потоке', 60, false, NULL),
    ('@Async оборачивает метод в новую JVM-нить без доступа к памяти приложения', 60, false, NULL),

    ('Сразу при завершении вложенного метода, а не в конце внешнего', 61, true,
        'Proxy приостанавливает T1, открывает T2, и при возврате из метода T2 коммитится немедленно. Если потом T1 откатится — изменения T2 останутся в БД.'),
    ('Только в конце внешнего @Transactional метода', 61, false, NULL),
    ('После flush всех EntityManager контекста', 61, false, NULL),
    ('REQUIRES_NEW вообще не коммитится сам — его коммитит внешняя транзакция', 61, false, NULL),

    ('Паттерн «REQUIRES_NEW + try-catch»: risky-операция в отдельной транзакции, исключение глотается', 62, true,
        'REQUIRES_NEW изолирует откат risky-операции от основной транзакции, а try-catch не даёт исключению пометить основную rollback-only. Альтернативы: noRollbackFor, NESTED (savepoint), TransactionTemplate.'),
    ('Достаточно try-catch вокруг вложенного REQUIRED-метода', 62, false, NULL),
    ('Выключить @Transactional на внешнем методе', 62, false, NULL),
    ('Такая ситуация неразрешима — откат неизбежен', 62, false, NULL);

-- 63-72: Isolation Levels
INSERT INTO questions (title) VALUES
    ('Объясните Dirty Read с примером'),
    ('Объясните Non-Repeatable Read с примером'),
    ('Объясните Phantom Read с примером'),
    ('Чем Dirty Read отличается от Lost Update?'),
    ('Какой уровень изоляции в PostgreSQL по умолчанию?'),
    ('В чём разница Pessimistic и Optimistic Locking?'),
    ('Что такое MVCC?'),
    ('Что произойдёт при конфликте с @Version (optimistic locking)?'),
    ('Делается ли retry автоматически при OptimisticLockException?'),
    ('Два пользователя одновременно списывают с одного счёта. Какую блокировку выбрать?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Транзакция читает незакоммиченные изменения другой; решается READ_COMMITTED и выше', 63, true,
        'T1 меняет сумму, T2 читает её до коммита T1, T1 откатывается — T2 работала с «грязными» данными. Предотвращается с READ_COMMITTED.'),
    ('Два SELECT в одной транзакции вернули разные данные', 63, false, NULL),
    ('Два SELECT вернули разное количество строк', 63, false, NULL),
    ('Обе транзакции пишут одну строку, одно обновление теряется', 63, false, NULL),

    ('Два одинаковых SELECT вернули разные значения строки, т.к. другая транзакция закоммитила UPDATE', 64, true,
        'T1 читает строку, T2 обновляет её и коммитит, повторный SELECT в T1 видит новое значение. Предотвращается REPEATABLE_READ и выше.'),
    ('Транзакция дважды вставила одну строку', 64, false, NULL),
    ('Чтение незакоммиченных данных другой транзакции', 64, false, NULL),
    ('Обновление одной транзакции перезаписано другой', 64, false, NULL),

    ('Два SELECT с одним условием вернули разное число строк — появилась/исчезла строка', 65, true,
        'T1 выбирает строки по условию, T2 вставляет новую подходящую строку и коммитит, повторный SELECT в T1 видит «фантом». Полностью решается SERIALIZABLE.'),
    ('Чтение незакоммиченных данных', 65, false, NULL),
    ('Повторное чтение строки даёт другое значение', 65, false, NULL),
    ('Обновление теряется из-за параллельной записи', 65, false, NULL),

    ('Dirty Read — чтение незакоммиченного; Lost Update — две записи, одна затирается', 66, true,
        'Dirty Read — проблема ЧТЕНИЯ чужих незафиксированных изменений. Lost Update — проблема ЗАПИСИ: T1 и T2 читают одно значение, обе пишут, вторая перезаписывает первую — обновление потеряно.'),
    ('Это синонимы одной проблемы', 66, false, NULL),
    ('Dirty Read — про запись, Lost Update — про чтение', 66, false, NULL),
    ('Lost Update возникает только на SERIALIZABLE', 66, false, NULL),

    ('READ_COMMITTED', 67, true,
        'PostgreSQL по умолчанию READ_COMMITTED (решает Dirty Read). MySQL/InnoDB по умолчанию REPEATABLE_READ. PostgreSQL имеет и уровни изоляции, и MVCC как механизм их реализации.'),
    ('REPEATABLE_READ', 67, false, NULL),
    ('SERIALIZABLE', 67, false, NULL),
    ('READ_UNCOMMITTED', 67, false, NULL),

    ('Optimistic — без блокировок, проверка версии на UPDATE, при редких конфликтах; Pessimistic — блокировка строки FOR UPDATE', 68, true,
        'Optimistic (@Version): UPDATE ... WHERE version=? — 0 строк → исключение; хорош при низкой contention. Pessimistic: SELECT ... FOR UPDATE держит блокировку строки до конца транзакции; при высокой contention, но рискует deadlock.'),
    ('Optimistic блокирует строки, Pessimistic — таблицы целиком', 68, false, NULL),
    ('Pessimistic не гарантирует согласованность', 68, false, NULL),
    ('Optimistic работает только в MySQL', 68, false, NULL),

    ('Механизм многоверсионности: читатели видят снимок закоммиченных данных и не блокируют писателей', 69, true,
        'MVCC хранит несколько версий строки: каждый SELECT видит снимок на момент старта (или стейтмента). Чтение не блокирует запись и наоборот — основа isolation в PostgreSQL, MySQL InnoDB, Oracle.'),
    ('Система глобальных блокировок таблиц на время транзакции', 69, false, NULL),
    ('Кэш запросов на уровне JDBC-драйвера', 69, false, NULL),
    ('Алгоритм репликации между узлами БД', 69, false, NULL),

    ('UPDATE с WHERE version=<старая> затронет 0 строк, Hibernate бросит OptimisticLockException, транзакция откатится', 70, true,
        'Проверка встроена в UPDATE: если версия в БД изменилась, условие WHERE не совпадает, обновляется 0 строк — это НЕ «тихий пропуск», а исключение (ObjectOptimisticLockingFailureException) и откат транзакции.'),
    ('Hibernate молча перезапишет чужие изменения', 70, false, NULL),
    ('Приложение зависнет в ожидании блокировки', 70, false, NULL),
    ('Автоматически поднимется уровень изоляции до SERIALIZABLE', 70, false, NULL),

    ('Нет: retry делается вручную или через @Retryable, с перезагрузкой сущности и пере-применением изменений', 71, true,
        'Повторный save() того же stale-объекта снова упадёт. Нужно перечитать свежую версию из БД, заново применить изменения и повторить (лимит попыток или @Retryable + @EnableRetry).'),
    ('Да, Hibernate делает до 3 попыток по умолчанию', 71, false, NULL),
    ('Да, если включён readOnly = false', 71, false, NULL),
    ('Retry есть только в PostgreSQL-драйвере', 71, false, NULL),

    ('Pessimistic (SELECT FOR UPDATE), если конфликты частые; optimistic при редких конфликтах', 72, true,
        'Высокая contention (частые одновременные правки одного счёта) → пессимистичная блокировка: второй запрос честно ждёт. Optimistic при частых конфликтах даст retry-шторм.'),
    ('Только Optimistic — он всегда быстрее', 72, false, NULL),
    ('Уровень изоляции READ_UNCOMMITTED без блокировок', 72, false, NULL),
    ('Никакой: БД сама разрулит без блокировок', 72, false, NULL);

-- 73-77: AOP
INSERT INTO questions (title) VALUES
    ('Что такое cross-cutting concerns? Приведите примеры'),
    ('Какие типы Advice бывают?'),
    ('Как Spring использует AOP под капотом?'),
    ('В чём разница @Around и @Before?'),
    ('Что такое Pointcut?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Функциональность, размазанная по многим модулям: логирование, безопасность, транзакции, метрики', 73, true,
        'Cross-cutting concerns нельзя изолировать в один класс без AOP — они касаются многих слоёв. AOP выносит их в аспекты: logging, security, @Transactional, кэширование, аудит.'),
    ('Ошибки, пересекающиеся между модулями системы', 73, false, NULL),
    ('Общие сущности БД, используемые несколькими таблицами', 73, false, NULL),
    ('Зависимости, общие для фронтенда и бэкенда', 73, false, NULL),

    ('@Before, @After, @AfterReturning, @AfterThrowing, @Around', 74, true,
        'Advice — действие аспекта в точке сопряжения: @Before до, @After после (finally), @AfterReturning при успехе, @AfterThrowing при исключении, @Around — оборачивает вызов целиком.'),
    ('Sync, Async, Deferred', 74, false, NULL),
    ('GET, POST, PUT, DELETE', 74, false, NULL),
    ('Compile-time, Load-time, Runtime', 74, false, NULL),

    ('@Transactional, @Async, @Cacheable, Security — всё реализовано через AOP-прокси', 75, true,
        'Spring оборачивает бины в прокси (JDK или CGLIB); вызовы перехватываются, и аннотированное поведение (транзакции, кэш, асинхронность, проверка прав) выполняется как advice. Работает только для вызовов извне бина.'),
    ('AOP используется только для логирования через LoggingAspect', 75, false, NULL),
    ('Spring компилирует аннотации в байт-код при сборке', 75, false, NULL),
    ('AOP в Spring применяется только к контроллерам', 75, false, NULL),

    ('@Around оборачивает вызов, может управлять им и менять результат; @Before только выполняется до него', 76, true,
        '@Around получает ProceedingJoinPoint, сам решает, вызывать ли pjp.proceed(), засекает время, может подменить результат или проглотить исключение. @Before лишь выполняет код перед методом и на результат не влияет.'),
    ('@Before работает до транзакции, @Around — после', 76, false, NULL),
    ('Разница только в порядке объявления в классе', 76, false, NULL),
    ('@Around доступен только в Spring Boot', 76, false, NULL),

    ('Выражение, определяющее, к каким join point применить advice', 77, true,
        'Pointcut — предикат выбора точек: execution(* com.example.service..*.*(..)) — все методы пакета service. Join point — конкретная точка выполнения, Pointcut — правило их отбора.'),
    ('Конкретная точка выполнения программы', 77, false, NULL),
    ('Класс, содержащий advice-методы', 77, false, NULL),
    ('Механизм создания прокси-классов', 77, false, NULL);

-- 78-92: Spring Security (детальные вопросы)
INSERT INTO questions (title) VALUES
    ('В чём разница authentication и authorization?'),
    ('Как работает JWT authentication в Spring Security?'),
    ('Что такое SecurityContext и как он хранится?'),
    ('В чём разница @PreAuthorize и @Secured?'),
    ('Как ограничить доступ к эндпоинту по роли?'),
    ('Что делает OncePerRequestFilter?'),
    ('В каком порядке выполняются SecurityFilterChain и @PreAuthorize?'),
    ('Что вернёт getAuthentication() для неавторизованного пользователя?'),
    ('В чём разница hasRole(''ADMIN'') и hasAuthority(''ROLE_ADMIN'')?'),
    ('Может ли @Service использовать SecurityContextHolder без SecurityFilterChain?'),
    ('Сработает ли @Secured при self-invocation через this?'),
    ('Что ответит Spring Security на истёкший JWT?'),
    ('Вернёт ли @Async-метод Authentication из request?'),
    ('Как Spring выбирает между несколькими SecurityFilterChain?'),
    ('Почему SessionCreationPolicy.STATELESS не отключает SecurityContext?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Authentication — проверка КТО ты; authorization — что тебе РАЗРЕШЕНО. Всегда сначала authentication', 78, true,
        'Authentication проверяет личность (login/password, JWT, OAuth). Authorization проверяет права (roles, permissions). Нельзя проверить права, не зная, кто ты.'),
    ('Authentication — права доступа, authorization — вход в систему', 78, false, NULL),
    ('Это одно и то же в разных терминах', 78, false, NULL),
    ('Authentication работает только с JWT, authorization — только с сессиями', 78, false, NULL),

    ('Фильтр валидирует токен из заголовка Authorization, извлекает пользователя/роли и кладёт Authentication в SecurityContext', 79, true,
        '1) POST /auth/login → сервер выдаёт JWT. 2) Клиент шлёт Authorization: Bearer <token>. 3) JwtAuthenticationFilter валидирует подпись/exp, строит Authentication и кладёт в SecurityContext. 4) Запрос идёт дальше уже аутентифицированным.'),
    ('JWT хранится в HTTP-сессии, Spring сам проверяет его при каждом запросе', 79, false, NULL),
    ('Spring проверяет JWT через запрос к внешнему auth-серверу при каждом вызове', 79, false, NULL),
    ('JWT передаётся в cookie, и валидацию выполняет браузер', 79, false, NULL),

    ('Объект с текущей Authentication; хранится в ThreadLocal через SecurityContextHolder', 80, true,
        'Каждый поток имеет свой SecurityContext (MODE_THREADLOCAL по умолчанию; есть INHERITABLETHREADLOCAL и GLOBAL). Фильтр заполняет его на запрос, в конце — очищает.'),
    ('SecurityContext хранится в HTTP-сессии на сервере', 80, false, NULL),
    ('SecurityContext — это JWT-токен на клиенте', 80, false, NULL),
    ('SecurityContext — глобальный singleton для всего приложения', 80, false, NULL),

    ('@PreAuthorize поддерживает SpEL и параметры метода; @Secured — только роли', 81, true,
        '@Secured("ROLE_ADMIN") — простая проверка роли. @PreAuthorize — SpEL: hasRole(), доступ к аргументам #param, @PostAuthorize по результату. Рекомендуется @PreAuthorize.'),
    ('@Secured новее и мощнее @PreAuthorize', 81, false, NULL),
    ('@PreAuthorize работает только на контроллерах', 81, false, NULL),
    ('Разница только в имени пакета', 81, false, NULL),

    ('SecurityFilterChain: requestMatchers(...).hasRole("ADMIN"); или метод с @PreAuthorize("hasRole(''ADMIN'')")', 82, true,
        'URL-уровень: http.authorizeHttpRequests(... .requestMatchers("/api/admin/**").hasRole("ADMIN") ...). Метод-уровень: @PreAuthorize("hasRole(''ADMIN'')"). На практике используют оба: общие правила в цепочке, точечные — на методах.'),
    ('Только через web.xml с секцией security-constraint', 82, false, NULL),
    ('Доступ по роли настраивается в application.yml', 82, false, NULL),
    ('Роли проверяются только вручную внутри метода контроллера', 82, false, NULL),

    ('Гарантирует, что фильтр выполнится ровно один раз на HTTP-запрос, даже при forward/include', 83, true,
        'Базовый класс для JWT-валидации, логирования, rate limiting. Обычный фильтр может выполниться многократно при dispatcher forward/include; OncePerRequestFilter защищает от этого.'),
    ('Выполняет фильтр один раз при старте приложения', 83, false, NULL),
    ('Кэширует ответ, чтобы фильтр не запускался повторно', 83, false, NULL),
    ('Ограничивает частоту запросов к эндпоинту', 83, false, NULL),

    ('Сначала SecurityFilterChain (фильтры), потом @PreAuthorize (method security)', 84, true,
        'Это разные уровни: если в цепочке denyAll — запрос не дойдёт до метода. Если permitAll — дойдёт, и проверку выполнит @PreAuthorize. Одно не заменяет другое.'),
    ('Сначала @PreAuthorize, потом цепочка фильтров', 84, false, NULL),
    ('Они выполняются параллельно', 84, false, NULL),
    ('Порядок не определён и зависит от JVM', 84, false, NULL),

    ('AnonymousAuthenticationToken с principal = "anonymousUser" (не null)', 85, true,
        'Для неавторизованного контекст содержит AnonymousAuthenticationToken с ROLE_ANONYMOUS и isAuthenticated() = false. Проверять надо instanceof / isAuthenticated(), а не null.'),
    ('null — контекст пуст', 85, false, NULL),
    ('UnauthorizedException', 85, false, NULL),
    ('GuestAuthentication с ролью ROLE_GUEST', 85, false, NULL),

    ('Они эквивалентны: hasRole(''ADMIN'') ищет authority "ROLE_ADMIN"', 86, true,
        'hasRole автоматически добавляет префикс ROLE_: hasRole(''ADMIN'') == hasAuthority(''ROLE_ADMIN''). Для кастомных authorities без префикса используйте hasAuthority.'),
    ('hasRole проверяет две роли сразу, hasAuthority — одну', 86, false, NULL),
    ('hasRole работает только с сессиями, hasAuthority — с JWT', 86, false, NULL),
    ('hasAuthority устарела, её заменила hasRole', 86, false, NULL),

    ('Да, если request прошёл через security-фильтры: SecurityContext в ThreadLocal того же потока', 87, true,
        'SecurityContext живёт в ThreadLocal потока обработки запроса — @Service читает его напрямую. Не работает в @Scheduled/@Async (другие потоки). Boot даже без явной конфигурации поднимает default SecurityFilterChain.'),
    ('Нет, SecurityContextHolder доступен только в контроллерах', 87, false, NULL),
    ('Только если вручную передать Authentication аргументом', 87, false, NULL),
    ('Только при включённой HTTP-сессии', 87, false, NULL),

    ('Нет: self-invocation обходит AOP-прокси, как и с @Transactional', 88, true,
        '@Secured/@PreAuthorize реализованы через AOP — вызов через this идёт мимо прокси. Решения: разнести по бинам, инжектить self через @Lazy или брать бин из контекста.'),
    ('Да, работает всегда', 88, false, NULL),
    ('Да, но только для public методов', 88, false, NULL),
    ('Код не скомпилируется', 88, false, NULL),

    ('Зависит от реализации фильтра: пропустить как anonymous (→401), явно вернуть 401 или бросить JwtException', 89, true,
        'Варианты: не устанавливать Authentication → Anonymous → 401; фильтр сам пишет 401; выбросить JwtException → ExceptionTranslationFilter → AuthenticationEntryPoint → 401. Единого поведения нет.'),
    ('Всегда HTTP 403 Forbidden', 89, false, NULL),
    ('Spring автоматически обновит токен через refresh', 89, false, NULL),
    ('Токен молча игнорируется, запрос выполняется', 89, false, NULL),

    ('Нет: @Async выполняется в другом потоке, а SecurityContext — в ThreadLocal', 90, true,
        'Новый поток из пула не видит ThreadLocal родителя. Решения: передать Authentication аргументом, DelegatingSecurityContextAsyncTaskExecutor или TaskDecorator. MODE_INHERITABLETHREADLOCAL не работает для пулов переиспользуемых потоков.'),
    ('Да, SecurityContext автоматически копируется в async-потоки', 90, false, NULL),
    ('Да, если включён STATELESS', 90, false, NULL),
    ('Метод упадёт с SecurityException', 90, false, NULL),

    ('Через @Order на бинах и/или securityMatcher: цепочка с совпавшим matcher обрабатывает запрос', 91, true,
        'Несколько SecurityFilterChain бинов: request проверяется против securityMatcher каждой цепочки в порядке @Order; первая подходящая обрабатывает запрос целиком. @Primary для SecurityFilterChain не используется.'),
    ('Случайно, порядок бинов не важен', 91, false, NULL),
    ('Все цепочки выполняются последовательно друг за другом', 91, false, NULL),
    ('Выбирает пользователь в заголовке запроса', 91, false, NULL),

    ('SecurityContext ≠ HTTP-сессия: он хранится в ThreadLocal и заполняется на каждый запрос', 92, true,
        'STATELESS запрещает создавать HTTP-сессию, но SecurityContextHolder по-прежнему кладёт Authentication в ThreadLocal на время запроса. Поэтому JWT-приложения продолжают работать без сессий.'),
    ('STATELESS полностью отключает Spring Security', 92, false, NULL),
    ('SecurityContext хранится в HTTP-сессии, поэтому STATELESS его убивает', 92, false, NULL),
    ('STATELESS работает только вместе с OAuth2', 92, false, NULL);

-- 93-101: @Value vs @ConfigurationProperties
INSERT INTO questions (title) VALUES
    ('В чём разница @Value и @ConfigurationProperties?'),
    ('Как использовать @Value со значением по умолчанию?'),
    ('Как валидировать @ConfigurationProperties?'),
    ('Что такое relaxed binding в @ConfigurationProperties?'),
    ('Когда использовать @Value, а когда @ConfigurationProperties?'),
    ('Поддерживает ли @Value SpEL?'),
    ('Что делает @EnableConfigurationProperties?'),
    ('Как использовать record с @ConfigurationProperties?'),
    ('В чём разница @Value("' || '$' || '{prop:default}") и @Value("#{prop ?: default}")?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('@Value — одно значение (нетипобезопасно); @ConfigurationProperties — типобезопасная привязка группы свойств к POJO', 93, true,
        '@Value: одиночные значения, SpEL, ошибки опечаток — только в runtime. @ConfigurationProperties: объект с типами, валидация @Validated, relaxed binding, ошибки при старте.'),
    ('@Value работает только с .properties, @ConfigurationProperties — только с YAML', 93, false, NULL),
    ('@ConfigurationProperties медленнее и не рекомендуется', 93, false, NULL),
    ('Это синонимы для разных версий Spring', 93, false, NULL),

    ('@Value("' || '$' || '{email.port:587}") — после двоеточия указывается default', 94, true,
        'Синтаксис $' || '{ключ:значениеПоУмолчанию}: если свойства нет в конфигурации, используется значение после двоеточия.'),
    ('@Value(value = "email.port", default = 587)', 94, false, NULL),
    ('Default задаётся в поле: private int port = 587 — @Value его сохранит', 94, false, NULL),
    ('@Value не поддерживает значения по умолчанию', 94, false, NULL),

    ('Через @Validated на классе и JSR-303 аннотации (@NotBlank, @Min) на полях', 95, true,
        '@ConfigurationProperties + @Validated: при старте Spring проверит ограничения (jakarta.validation). Невалидная конфигурация упадёт на старте, а не в runtime.'),
    ('Валидация конфигурации невозможна, только ручные проверки', 95, false, NULL),
    ('Через @Valid на каждом injected поле', 95, false, NULL),
    ('Через XML-схему конфигурации', 95, false, NULL),

    ('Свойства привязываются независимо от формата имени: email-host, email_host, EMAIL_HOST, emailHost', 96, true,
        'Relaxed binding сопоставляет kebab-case, snake_case, UPPER_CASE (env vars) и camelCase с одним и тем же полем emailHost — удобно для env-переменных.'),
    ('Spring автоматически исправляет опечатки в именах свойств', 96, false, NULL),
    ('Это поиск свойства по частичному совпадению имени', 96, false, NULL),
    ('Это привязка свойств между разными профилями', 96, false, NULL),

    ('@Value — для единичных значений и SpEL; @ConfigurationProperties — для структурированных групп (БД, email, API)', 97, true,
        'Одно-два значения — @Value. Связная группа настроек — @ConfigurationProperties: типобезопасность, валидация, автодокументация, переиспользование.'),
    ('Всегда @Value — он универсальнее', 97, false, NULL),
    ('Всегда @ConfigurationProperties, @Value устарел', 97, false, NULL),
    ('@Value — в тестах, @ConfigurationProperties — в проде', 97, false, NULL),

    ('Да: @Value("#{T(Math).random() * 100.0}") — SpEL-выражения поддерживаются', 98, true,
        '@Value поддерживает SpEL в синтаксисе #{...}: вычисления, ссылки на бины, T()-обращения к типам. Placeholder-подстановка — $' || '{...}.'),
    ('Нет, @Value работает только с плейсхолдерами', 98, false, NULL),
    ('Только в Spring Boot 3+', 98, false, NULL),
    ('Только вместе с @ConfigurationProperties', 98, false, NULL),

    ('Регистрирует POJO с @ConfigurationProperties как бин без @Component', 99, true,
        '@EnableConfigurationProperties(EmailProperties.class) включает привязку и регистрирует бин — способ без @Component (например, для constructor binding или чужих классов).'),
    ('Включает кэширование значений @Value', 99, false, NULL),
    ('Подключает файл application.properties вручную', 99, false, NULL),
    ('Сканирует classpath на предмет .properties файлов', 99, false, NULL),

    ('Record с constructor binding: @ConfigurationProperties на record, setters не нужны', 100, true,
        'Record иммутабелен, значения привязываются через конструктор (constructor binding). При этом @Component не нужен — регистрация через @EnableConfigurationProperties или @ConfigurationPropertiesScan.'),
    ('Никак: records не поддерживаются, нужен класс с getters/setters', 100, false, NULL),
    ('Через @ConfigurationProperties + @Autowired в record', 100, false, NULL),
    ('Только с ручным парсингом Environment', 100, false, NULL),

    ('Первое — placeholder с default; второе — SpEL c Elvis-оператором (в SpEL нужно $' || '{prop})', 101, true,
        '$' || '{prop:default} — подстановка свойства со значением по умолчанию. #{...} — SpEL: внутри ссылка на свойство через @ConfigurationProperties-бин или systemProperties; ?: — Elvis. Смешивать синтаксисы нельзя.'),
    ('Это два синонимичных синтаксиса', 101, false, NULL),
    ('SpEL-вариант быстрее, т.к. кэшируется', 101, false, NULL),
    ('Второй вариант читает только переменные окружения', 101, false, NULL);

-- 102-104: @RestController vs @Controller
INSERT INTO questions (title) VALUES
    ('Что делает @ResponseBody?'),
    ('Когда использовать @Controller вместо @RestController?'),
    ('Как Spring преобразует объект в JSON?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Указывает, что возвращаемое значение метода — тело ответа (JSON/XML), а не имя view-шаблона', 102, true,
        'Без @ResponseBody возвращаемая строка трактуется как имя шаблона. С ней значение сериализуется в тело HTTP-ответа через HttpMessageConverter. @RestController включает это для всех методов.'),
    ('Пропускает запрос через цепочку security-фильтров', 102, false, NULL),
    ('Включает кэширование HTTP-ответов', 102, false, NULL),
    ('Регистрирует метод как обработчик GET-запросов', 102, false, NULL),

    ('Когда приложение отдаёт серверные view (Thymeleaf, JSP): админки, SSR, legacy', 103, true,
        '@Controller + view-шаблоны — для серверного рендеринга HTML. Для JSON-API с отделённым фронтендом — @RestController.'),
    ('Всегда — @RestController устарел', 103, false, NULL),
    ('Только для WebSocket-эндпоинтов', 103, false, NULL),
    ('Когда нужно вернуть JSON без шаблонов', 103, false, NULL),

    ('Через HttpMessageConverter: Jackson (MappingJackson2HttpMessageConverter) сериализует объект по геттерам', 104, true,
        'Spring подбирает converter по content-type: Jackson сериализует POJO в JSON (по геттерам/аннотациям @JsonProperty), Gson и т.п. — аналогично. Конвертеры регистрируются автоматически в Boot.'),
    ('Через рефлексию прямо в HttpServletResponse без библиотек', 104, false, NULL),
    ('Через XStream в XML, потом конвертацию в JSON', 104, false, NULL),
    ('Никак: объекты надо сериализовать вручную', 104, false, NULL);

-- 105-110: Externalized Configuration
INSERT INTO questions (title) VALUES
    ('application.properties и application.yml задают один ключ разными значениями. Что произойдёт?'),
    ('Какой формат приоритетнее, если оба файла в одном location?'),
    ('Что имеет высший приоритет среди источников конфигурации?'),
    ('В чём плюс YAML перед properties?'),
    ('Как подключить дополнительный конфиг-файл?'),
    ('Перекрывает ли application-prod.yml значения из application.yml?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Сработает значение из .properties — оно «затенит» yml; рекомендация — держать один формат', 105, true,
        'В одном location .properties имеет более высокий приоритет. Частая ловушка: кажется, что конфиг в yml, а реально работает значение из properties.'),
    ('Оба значения загрузятся, Spring выберет случайное', 105, false, NULL),
    ('Приложение упадёт на старте с конфликтом ключей', 105, false, NULL),
    ('YAML имеет приоритет над properties', 105, false, NULL),

    ('.properties (при совпадении ключа)', 106, true,
        'Официальная рекомендация — «use a consistent format»: один формат на проект. Если оба присутствуют, .properties побеждает.'),
    ('.yml — он новее', 106, false, NULL),
    ('Файл, отсортированный последним по алфавиту', 106, false, NULL),
    ('Форматы равнозначны', 106, false, NULL),

    ('Command-line аргументы, затем переменные окружения, затем профили вне/внутри jar', 107, true,
        'Порядок: command line args > env vars > application-{profile} вне jar > application вне jar > application-{profile} внутри jar > application внутри jar > @PropertySource > дефолты. Поздний источник перекрывает ранний.'),
    ('Значения по умолчанию из кода', 107, false, NULL),
    ('application.yml внутри jar — он загружается последним', 107, false, NULL),
    ('@PropertySource имеет высший приоритет', 107, false, NULL),

    ('Иерархическая структура, списки, несколько профилей в одном файле через ---', 108, true,
        'YAML даёт вложенность без префиксов-ключей, списки через дефис и мультипрофильный файл (--- + spring.config.activate.on-profile). Но чувствителен к отступам.'),
    ('YAML быстрее парсится и снижает потребление памяти', 108, false, NULL),
    ('YAML поддерживает шифрование значений из коробки', 108, false, NULL),
    ('Преимуществ нет — YAML устарел', 108, false, NULL),

    ('Через spring.config.import в application.yml', 109, true,
        'spring.config.import: classpath:extra.yml — импортируемый файл перекрывает импортирующий. Удобно для секретов и модульных конфигов.'),
    ('Только через @PropertySource в коде', 109, false, NULL),
    ('Дополнительные файлы подключаются флагом -Dconfig.extra', 109, false, NULL),
    ('Spring читает все yml-файлы classpath автоматически', 109, false, NULL),

    ('Да: profile-specific конфиг всегда перекрывает non-profile', 110, true,
        'application-{profile}.yml имеет более высокий приоритет, чем application.yml (и внутри jar, и вне). Активируется через spring.profiles.active.'),
    ('Нет, application.yml всегда главнее', 110, false, NULL),
    ('Только если prod указан первым в списке профилей', 110, false, NULL),
    ('Профили нельзя комбинировать с базовым файлом', 110, false, NULL);

-- ============================================================================
-- 3. DATA & JPA
-- ============================================================================

-- 111-123: JPA vs Hibernate
INSERT INTO questions (title) VALUES
    ('Что такое JPA?'),
    ('В чём разница JPA и Hibernate?'),
    ('Что такое Spring Data JPA и зачем он нужен?'),
    ('Что такое JPQL и чем он отличается от SQL?'),
    ('Какие реализации JPA существуют?'),
    ('Кто генерирует SQL — JPA, Hibernate или Spring Data JPA?'),
    ('В каком пакете находятся аннотации JPA в Spring Boot 3+?'),
    ('Чем отличается JDBC от JPA?'),
    ('В чём разница L1 и L2 кеша?'),
    ('Как включить L2 кеш?'),
    ('Как написать native SQL через @Query?'),
    ('Можно ли в JPQL/HQL использовать таблицы?'),
    ('Чем отличается JPQL от HQL?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Спецификация (стандарт) ORM для Java: интерфейсы EntityManager, аннотации @Entity и т.д.', 111, true,
        'JPA — спецификация, описывающая API маппинга объектов на таблицы: EntityManager, @Entity, JPQL. Реализации — Hibernate, EclipseLink. Появилась в 2006, частично на основе Hibernate.'),
    ('Библиотека для подключения к PostgreSQL', 111, false, NULL),
    ('Встроенная БД для тестов', 111, false, NULL),
    ('Генератор миграций для схем БД', 111, false, NULL),

    ('JPA — спецификация (интерфейсы), Hibernate — её реализация с доп. фичами (L2-кеш, lazy loading, HQL)', 112, true,
        'JPA описывает ЧТО (стандарт), Hibernate — КАК (реализация + собственные расширения: HQL, L1/L2 кеш, batch). Код против JPA-интерфейсов можно перенести на другую реализацию.'),
    ('Hibernate — старое название JPA', 112, false, NULL),
    ('JPA быстрее, Hibernate безопаснее', 112, false, NULL),
    ('JPA работает только с MySQL, Hibernate — с любыми БД', 112, false, NULL),

    ('Модуль Spring над JPA: репозитории с автогенерацией запросов по именам методов и @Query', 113, true,
        'Spring Data JPA убирает boilerplate-DAO: JpaRepository даёт CRUD, методы типа findByUsername генерируются по конвенции, @Query — для сложных случаев. Иерархия: Spring Data JPA → JPA → Hibernate → JDBC → БД.'),
    ('Реализация JPA от Spring, конкурирующая с Hibernate', 113, false, NULL),
    ('Пул соединений для JPA', 113, false, NULL),
    ('Фреймворк миграций схем для JPA-сущностей', 113, false, NULL),

    ('JPQL работает с сущностями и их полями, SQL — с таблицами и колонками', 114, true,
        'JPQL: SELECT u FROM User u WHERE u.username = :name — объекты и поля классов. Hibernate переводит JPQL в SQL конкретной БД через dialect. В JPQL нельзя обращаться к таблицам напрямую.'),
    ('JPQL — диалект SQL для PostgreSQL', 114, false, NULL),
    ('JPQL работает быстрее SQL за счёт кэша', 114, false, NULL),
    ('Это синонимы для объектных запросов', 114, false, NULL),

    ('Hibernate, EclipseLink, OpenJPA', 115, true,
        'JPA — стандарт, его реализуют Hibernate (самая популярная), EclipseLink (референсная), OpenJPA и др.'),
    ('Spring Data JPA и JDBC Template', 115, false, NULL),
    ('Tomcat и Jetty', 115, false, NULL),
    ('JPA имеет единственную реализацию — Hibernate', 115, false, NULL),

    ('Hibernate — JPA лишь описывает API, Spring Data JPA — только фасад над репозиториями', 116, true,
        'Spring Data JPA генерирует JPQL/методы, но SQL строит и выполняет Hibernate (провайдер JPA) через JDBC.'),
    ('Spring Data JPA', 116, false, NULL),
    ('JPA через EntityManager', 116, false, NULL),
    ('PostgreSQL JDBC-драйвер', 116, false, NULL),

    ('jakarta.persistence.* (раньше был javax.persistence.*)', 117, true,
        'С переходом на Jakarta EE 9 (Spring Boot 3) пакет javax.persistence переименован в jakarta.persistence.'),
    ('javax.persistence.*', 117, false, NULL),
    ('org.springframework.data.jpa.*', 117, false, NULL),
    ('org.hibernate.annotations.*', 117, false, NULL),

    ('JDBC — низкоуровневый API работы с SQL напрямую; JPA — ORM над ним: маппинг объектов, кеши, dirty checking', 118, true,
        'JDBC: руками пишешь SQL, маппишь ResultSet. JPA использует JDBC под капотом, но добавляет ORM: сущности, связи, кеш первого уровня, автоматические UPDATE.'),
    ('JDBC для PostgreSQL, JPA для MySQL', 118, false, NULL),
    ('Это синонимы', 118, false, NULL),
    ('JPA заменяет драйвер БД', 118, false, NULL),

    ('L1 — кеш сессии (EntityManager), обязателен; L2 — общий для всех сессий, опционален', 119, true,
        'L1 привязан к сессии, чистится при её закрытии, гарантирует identity и служит для dirty checking. L2 живёт с приложением, разделяется сессиями, требует провайдера (Ehcache, Caffeine, Redis).'),
    ('L1 на диске, L2 в памяти', 119, false, NULL),
    ('L1 для запросов, L2 для транзакций', 119, false, NULL),
    ('Разницы нет, это уровни вложенности коллекций', 119, false, NULL),

    ('Свойства hibernate.cache.use_second_level_cache=true + @Cacheable и @Cache(usage=...) на сущности', 120, true,
        'По умолчанию L2 выключен: включается свойством, на сущности ставится @org.hibernate.annotations.Cache с стратегией (READ_ONLY для справочников, READ_WRITE и т.д.), нужен провайдер кеша.'),
    ('L2 включается автоматически в Spring Boot 3', 120, false, NULL),
    ('Через @EnableCaching — это и есть L2 кеш Hibernate', 120, false, NULL),
    ('Через spring.datasource.cache=true', 120, false, NULL),

    ('@Query(value = "SELECT * FROM users WHERE ...", nativeQuery = true)', 121, true,
        'nativeQuery = true говорит, что запрос — чистый SQL с таблицами, а не JPQL. Нужен для спец. функций БД (например, RANDOM() в PostgreSQL).'),
    ('@Query("SELECT * FROM users") без доп. параметров', 121, false, NULL),
    ('Только через JdbcTemplate, в @Query это невозможно', 121, false, NULL),
    ('Через аннотацию @NativeQuery', 121, false, NULL),

    ('Нет: JPQL/HQL работают только с сущностями и полями классов', 122, true,
        'JPQL оперирует именами сущностей (User), а не таблиц (users). Для таблиц — native query. Hibernate переведёт JPQL в SQL сам.'),
    ('Да, Hibernate сам различает таблицы и сущности', 122, false, NULL),
    ('Только в HQL, не в JPQL', 122, false, NULL),
    ('Только для read-only запросов', 122, false, NULL),

    ('JPQL — стандарт JPA; HQL — расширение Hibernate, тоже работает с сущностями', 123, true,
        'HQL — предшественник и расширение JPQL: дополнительные возможности Hibernate (например, with-clause, some функции). Оба обращаются к сущностям, не к таблицам.'),
    ('HQL работает с таблицами, JPQL — с сущностями', 123, false, NULL),
    ('HQL — для PostgreSQL, JPQL — для Oracle', 123, false, NULL),
    ('JPQL устарел, его заменил HQL', 123, false, NULL);

-- 124-133: Entity Lifecycle
INSERT INTO questions (title) VALUES
    ('Назовите 4 состояния Entity по отношению к Persistence Context'),
    ('Что делает entityManager.persist()?'),
    ('Что делает entityManager.merge()?'),
    ('Что такое dirty checking и когда происходит?'),
    ('Почему изменения managed-сущности сохраняются без save()?'),
    ('Что будет, если проигнорировать результат merge()?'),
    ('Что делает Spring Data save() под капотом?'),
    ('Что такое CascadeType.ALL и включает ли он orphanRemoval?'),
    ('В чём разница CascadeType.REMOVE и orphanRemoval = true?'),
    ('Какое fetch-поведение по умолчанию у @ManyToOne и @OneToMany?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Transient, Managed, Detached, Removed', 124, true,
        'Transient — new, нет ID, Hibernate не знает. Managed — привязан к PC, отслеживается. Detached — был managed, PC закрыт. Removed — помечен remove(), удалится при flush.'),
    ('Open, Closed, ReadOnly, Locked', 124, false, NULL),
    ('New, Saved, Updated, Deleted', 124, false, NULL),
    ('Loaded, Cached, Flushed, Committed', 124, false, NULL),

    ('Переводит Transient → Managed: привязывает к Persistence Context, планирует INSERT', 125, true,
        'После persist сущность managed: изменения отслеживаются. С IDENTITY INSERT выполняется сразу (нужен ID), с SEQUENCE — откладывается до flush.'),
    ('Сразу выполняет DELETE', 125, false, NULL),
    ('Возвращает новую managed-копию сущности', 125, false, NULL),
    ('Сохраняет сущность в L2-кеш', 125, false, NULL),

    ('Переводит Detached → Managed, возвращая НОВУЮ managed-копию; исходный объект не меняется', 126, true,
        'merge() копирует состояние detached-объекта в managed-версию из PC и возвращает её. Дальше работать надо с возвращённым объектом — изменения исходного detached потеряются.'),
    ('Привязывает detached-объект к сессии на месте', 126, false, NULL),
    ('Удаляет сущность при следующем flush', 126, false, NULL),
    ('Создаёт сущность заново с новым ID', 126, false, NULL),

    ('Hibernate сравнивает managed-сущность со снапшотом при flush и сам генерирует UPDATE', 127, true,
        'При загрузке Hibernate хранит снимок сущности. На flush (при коммите, перед query) текущее состояние сравнивается со снимком — найденные изменения становятся UPDATE. Term: dirty checking.'),
    ('Разработчик вручную вызывает flush для каждой сущности', 127, false, NULL),
    ('Это механизм транзакций JDBC, не Hibernate', 127, false, NULL),
    ('Термин означает проверку dirty-полей через equals при insert', 127, false, NULL),

    ('Сущность managed: на коммите flush вызывает dirty checking и Hibernate выполняет UPDATE сам', 128, true,
        'Spring Data save() на managed-сущности внутри транзакции вообще лишний — dirty checking всё сделает. Условие: сущность в Managed-состоянии и открытая транзакция.'),
    ('Save() вызывается автоматически AOP-аспектом', 128, false, NULL),
    ('Изменения теряются: без save() Hibernate ничего не заметит', 128, false, NULL),
    ('Hibernate пересоздаёт сущность через merge', 128, false, NULL),

    ('Изменения потеряются: detached-копия не отслеживается, работать надо с возвращённым merge()-объектом', 129, true,
        'merge() не «оживляет» исходный объект — он возвращает новую managed-копию. Мутации исходного detached-объекта никуда не попадут.'),
    ('Ничего, объект и так managed', 129, false, NULL),
    ('Сессия закроется автоматически', 129, false, NULL),
    ('Сущность удалится при flush', 129, false, NULL),

    ('isNew → persist, есть ID → merge', 130, true,
        'SimpleJpaRepository.save проверяет isNew (по @Id/версии): новый — persist(), существующий — merge(). Внутри транзакции на managed-сущности save не нужен — dirty checking.'),
    ('Всегда persist', 130, false, NULL),
    ('Всегда merge', 130, false, NULL),
    ('Пишет нативный INSERT/UPDATE через JdbcTemplate', 130, false, NULL),

    ('Каскад всех операций (PERSIST, MERGE, REMOVE, REFRESH, DETACH); orphanRemoval НЕ включает', 131, true,
        'CascadeType.ALL распространяет операции EntityManager на связанные сущности, но orphanRemoval — отдельная настройка: cascade=ALL + orphanRemoval=true обычно ставят вместе для композиции. На fetch не влияет.'),
    ('ALL включает всё, включая orphanRemoval и fetch=EAGER', 131, false, NULL),
    ('Это режим блокировки таблицы', 131, false, NULL),
    ('ALL отключает каскадирование, остальные типы включают', 131, false, NULL),

    ('Триггер: REMOVE — вызов em.remove(parent); orphanRemoval — удаление child из коллекции', 132, true,
        'CascadeType.REMOVE распространяет em.remove() на детей (удаление агрегата). orphanRemoval удаляет «осиротевший» child при parent.getOptions().remove(child) — child не может жить без parent. Для композиции ставят оба.'),
    ('Это синонимы', 132, false, NULL),
    ('REMOVE удаляет только строку в БД, orphanRemoval — и кеш', 132, false, NULL),
    ('orphanRemoval работает только с EAGER-коллекциями', 132, false, NULL),

    ('@ManyToOne — EAGER; @OneToMany — LAZY', 133, true,
        'Дефолт JPA: ManyToOne/OneToOne — EAGER, OneToMany/ManyToMany — LAZY. Часто ManyToOne переопределяют на LAZY, чтобы избежать лишних JOIN и LazyInitializationException.'),
    ('Оба EAGER', 133, false, NULL),
    ('Оба LAZY', 133, false, NULL),
    ('@ManyToOne — LAZY, @OneToMany — EAGER', 133, false, NULL);

-- 134-138: LazyInitializationException
INSERT INTO questions (title) VALUES
    ('Что такое lazy loading в Hibernate?'),
    ('Почему возникает LazyInitializationException?'),
    ('Какие решения LazyInitializationException и какое рекомендуется?'),
    ('Почему Open Session in View — антипаттерн?'),
    ('В чём разница EAGER и LAZY?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Связанные данные загружаются из БД только при первом обращении, а не сразу с сущностью', 134, true,
        'LAZY-связь подменяется прокси: SQL выполняется при первом доступе к полю/коллекции, пока открыта сессия. Экономит запросы, но требует живой сессии.'),
    ('Данные загружаются в момент запроса целиком вместе со всеми связями', 134, false, NULL),
    ('Механизм кеширования сущностей между сессиями', 134, false, NULL),
    ('Отложенная запись изменений в БД', 134, false, NULL),

    ('Доступ к lazy-связи после закрытия сессии: прокси не может догрузить данные', 135, true,
        'Сущность стала detached (сессия закрыта, транзакция кончилась), обращение к lazy-полю — Hibernate не может выполнить SQL → LazyInitializationException.'),
    ('Когда сущность помечена @Entity(lazy = false)', 135, false, NULL),
    ('Из-за закрытого пула соединений', 135, false, NULL),
    ('Когда забыт @Transactional над методом', 135, false, NULL),

    ('JOIN FETCH / @EntityGraph в репозитории — рекомендуется; OSiV — антипаттерн; DTO projection', 136, true,
        'Правильное решение — загрузить нужные данные запросом: JOIN FETCH в JPQL или @EntityGraph. DTO projection — тоже хорошо. OSiV маскирует проблему, держа сессию открытой на весь запрос.'),
    ('Только добавить EAGER ко всем связям', 136, false, NULL),
    ('Обернуть доступ в synchronized', 136, false, NULL),
    ('Включить L2-кеш для коллекций', 136, false, NULL),

    ('Держит соединение БД занятым весь HTTP-запрос и маскирует N+1, скрывая проблемы производительности', 137, true,
        'OSiV открывает сессию на весь запрос: соединение занято даже когда данные уже загружены (пул может исчерпаться), а ленивые обращения в view генерируют незаметные N+1-запросы. Boot включает OSiV по умолчанию — стоит отключить.'),
    ('OSiV запрещён спецификацией JPA', 137, false, NULL),
    ('Он не работает с PostgreSQL', 137, false, NULL),
    ('Он отключает L1-кеш', 137, false, NULL),

    ('EAGER грузит связь сразу с сущностью; LAZY — отложенно при первом обращении', 138, true,
        'EAGER — JOIN/доп. запрос при загрузке (риск лишних данных и N+1). LAZY — прокси, запрос при доступе, но требует открытой сессии. По умолчанию: ManyToOne EAGER, OneToMany LAZY.'),
    ('EAGER для чтения, LAZY для записи', 138, false, NULL),
    ('EAGER кеширует, LAZY — нет', 138, false, NULL),
    ('EAGER работает в транзакции, LAZY — вне', 138, false, NULL);

-- 139-145: N+1 и EntityGraph
INSERT INTO questions (title) VALUES
    ('Объясните N+1 problem на примере'),
    ('Как решить N+1 через JOIN FETCH?'),
    ('В чём разница JOIN FETCH и @EntityGraph?'),
    ('Как @BatchSize помогает при N+1?'),
    ('Как включить лог SQL запросов Hibernate?'),
    ('Как определить @NamedEntityGraph?'),
    ('Когда @EntityGraph удобнее JOIN FETCH?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('1 запрос списка + N запросов ленивых связей для каждого элемента', 139, true,
        'SELECT всех вопросов (1) + SELECT topic для каждого вопроса (N). Обнаруживается по логу SQL: куча однотипных запросов в цикле. Решения: JOIN FETCH, @EntityGraph, @BatchSize, DTO projection.'),
    ('Один запрос выполняется N раз из-за отсутствия индекса', 139, false, NULL),
    ('Транзакция N раз перезапускается из-за конфликтов', 139, false, NULL),
    ('N потоков compete за одно соединение пула', 139, false, NULL),

    ('@Query("SELECT t FROM Topic t LEFT JOIN FETCH t.questions") — данные грузятся одним запросом', 140, true,
        'JOIN FETCH в JPQL заставляет Hibernate выполнить JOIN и заполнить коллекцию в том же SQL — вместо N отдельных запросов.'),
    ('Включить @BatchSize на репозитории', 140, false, NULL),
    ('Установить FetchType.EAGER на коллекции', 140, false, NULL),
    ('Выполнить запросы в отдельном пуле потоков', 140, false, NULL),

    ('JOIN FETCH — императивно в JPQL-запросе; @EntityGraph — декларативно на методе репозитория', 141, true,
        '@EntityGraph(attributePaths = {"questions"}) не требует менять JPQL, удобен поверх конвентных методов. JOIN FETCH гибче: условия, несколько коллекций, LEFT JOIN.'),
    ('JOIN FETCH кеширует результат, @EntityGraph — нет', 141, false, NULL),
    ('@EntityGraph работает только с нативным SQL', 141, false, NULL),
    ('Это синонимы', 141, false, NULL),

    ('Загружает ленивые связи пакетами по N штук (IN-запрос), сокращая N запросов до N/batch', 142, true,
        '@BatchSize(size = 10): вместо SELECT на каждую сущность — SELECT ... WHERE id IN (10 idов). Полностью проблему не решает, но смягчает.'),
    ('Кеширует запросы в L2 на batch-размер', 142, false, NULL),
    ('Объединяет INSERT в батчи', 142, false, NULL),
    ('Устанавливает fetch size JDBC-драйвера для курсоров', 142, false, NULL),

    ('logging.level.org.hibernate.SQL=DEBUG (+ org.hibernate.orm.jdbc.bind=TRACE для параметров)', 143, true,
        'show-sql=true печатает без параметров и без логгера; правильнее — DEBUG на org.hibernate.SQL, а в Hibernate 6 параметры — org.hibernate.orm.jdbc.bind=TRACE.'),
    ('spring.jpa.show-sql=true — единственный правильный способ', 143, false, NULL),
    ('Через EXPLAIN ANALYZE', 143, false, NULL),
    ('Лог SQL включается в PostgreSQL через log_statement', 143, false, NULL),

    ('@NamedEntityGraph на сущности + @EntityGraph(value = "...") на методе репозитория', 144, true,
        '@NamedEntityGraph(name = "Topic.questions", attributeNodes = @NamedAttributeNode("questions")) объявляется на Entity, ссылается по имени из @EntityGraph — позволяет переиспользовать графы.'),
    ('Через XML-файл entity-graphs.xml', 144, false, NULL),
    ('Только инлайн: @EntityGraph(attributePaths = ...) прямо на методе', 144, false, NULL),
    ('В application.yml в секции spring.jpa.entity-graphs', 144, false, NULL),

    ('Когда граф нужен поверх стандартных методов (findById, findAll) или графы шарятся между запросами', 145, true,
        'JOIN FETCH требует своего JPQL на каждый запрос; @EntityGraph навешивается на любой метод репозитория, включая именованные, и переиспользуется через @NamedEntityGraph.'),
    ('Когда нужно LEFT JOIN с условиями — это умеет только @EntityGraph', 145, false, NULL),
    ('Всегда — это новая замена JOIN FETCH', 145, false, NULL),
    ('Только с нативными запросами', 145, false, NULL);

-- 146-153: Batch Inserts & GenerationType
INSERT INTO questions (title) VALUES
    ('Что такое batch inserts и какой выигрыш они дают?'),
    ('Куда ставится @GeneratedValue и что будет, если поставить его на не-@Id поле?'),
    ('Почему IDENTITY ломает batch inserts, а SEQUENCE — нет?'),
    ('500 сущностей с IDENTITY и batch_size = 50: сколько INSERT уйдёт в БД?'),
    ('Как включить batch inserts в Spring Boot?'),
    ('Какая GenerationType рекомендуется для PostgreSQL и почему?'),
    ('Что такое allocationSize у SEQUENCE?'),
    ('Зависит ли batching от FetchType (LAZY/EAGER)?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Несколько INSERT отправляются одним пакетом вместо по одному; выигрыш 5-10x', 146, true,
        'JDBC-батч копит statements и отправляет одним round-trip. Работает, только если Hibernate может отложить INSERT до flush — что зависит от стратегии генерации ID.'),
    ('Вставка строк в несколько таблиц за раз', 146, false, NULL),
    ('Использование INSERT ... SELECT для копирования данных', 146, false, NULL),
    ('Многопоточная вставка через пул соединений', 146, false, NULL),

    ('На поле вместе с @Id; на не-@Id поле аннотация игнорируется', 147, true,
        '@GeneratedValue ставится только на поле PK в паре с @Id. На обычном поле она игнорируется: значения не генерируются, поле уйдёт в INSERT как обычная колонка (null/0).'),
    ('На классе сущности', 147, false, NULL),
    ('На getter любого поля', 147, false, NULL),
    ('В конструкторе сущности', 147, false, NULL),

    ('IDENTITY требует ID сразу → INSERT выполняется немедленно при persist; SEQUENCE даёт ID заранее → INSERT копится', 148, true,
        'С IDENTITY Hibernate вынужден выполнить INSERT прямо в persist() и взять сгенерированный ключ — батч не из чего копить. С SEQUENCE ID берётся из nextval до INSERT, INSERT откладывается до flush и батчится.'),
    ('IDENTITY медленнее считается в БД', 148, false, NULL),
    ('SEQUENCE несовместим с batch_size', 148, false, NULL),
    ('IDENTITY не поддерживается PostgreSQL', 148, false, NULL),

    ('500 — по одному INSERT на сущность, батч не формируется', 149, true,
        'IDENTITY стреляет INSERT немедленно при каждом persist(): batch_size игнорируется. Ловушка: настройка batch_size не «включает» батчинг сама по себе.'),
    ('10 — 500/50', 149, false, NULL),
    ('1 — всё одним пакетом', 149, false, NULL),
    ('50 — максимальный размер батча', 149, false, NULL),

    ('hibernate.jdbc.batch_size + order_inserts/order_updates; для PG ещё reWriteBatchedInserts=true в JDBC URL', 150, true,
        'spring.jpa.properties.hibernate.jdbc.batch_size=50, order_inserts=true группирует INSERT одной таблицы. reWriteBatchedInserts у PostgreSQL-драйвера переписывает батч в multi-row INSERT.'),
    ('Включается аннотацией @Batch(insert = true)', 150, false, NULL),
    ('Только через GenerationType.TABLE', 150, false, NULL),
    ('В Spring Boot батчинг включён по умолчанию', 150, false, NULL),

    ('SEQUENCE — быстрый и батчится; IDENTITY запрещает батчинг', 151, true,
        'Hibernate 6 на PostgreSQL @GeneratedValue без стратегии → SEQUENCE. TABLE тоже батчит, но медленный (блокировки таблицы-счётчика) — только для БД без sequences.'),
    ('IDENTITY — так проще всего', 151, false, NULL),
    ('TABLE — самый портируемый и быстрый', 151, false, NULL),
    ('AUTO — единственная работающая стратегия', 151, false, NULL),

    ('Сколько ID берётся за один nextval: allocationSize = 50 → блок из 50 ID за раз', 152, true,
        'Большой allocationSize уменьшает обращения к БД за ID (хай-ло паттерн), но оставляет дыры в нумерации и теряет ID при рестарте.'),
    ('Максимальное значение sequence', 152, false, NULL),
    ('Размер JDBC-батча для INSERT', 152, false, NULL),
    ('Шаг инкремента при каждом INSERT', 152, false, NULL),

    ('Нет: batching определяется стратегией генерации ID, а не типом загрузки связей', 153, true,
        'FetchType управляет ЧТЕНИЕМ связанных данных; batch INSERT — записью новых сущностей. Это независимые механизмы.'),
    ('Да, только с EAGER возможен батчинг', 153, false, NULL),
    ('Да, LAZY замедляет батчи из-за прокси', 153, false, NULL),
    ('Только LAZY включает batch inserts', 153, false, NULL);

-- ============================================================================
-- 4. JVM & PERFORMANCE
-- ============================================================================

-- 154-158: Heap vs Stack
INSERT INTO questions (title) VALUES
    ('Что хранится в Stack, а что в Heap?'),
    ('Что такое StackOverflowError?'),
    ('Из чего состоит Heap?'),
    ('Что такое Young Generation и Old Generation?'),
    ('Где хранятся static переменные?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Stack — фреймы методов (примитивы, ссылки); Heap — все объекты, разделяется потоками', 154, true,
        'Stack — LIFO-фреймы: локальные переменные (примитивы и ссылки), операнды. Heap — сами объекты, общие для всех потоков, чистится GC. Metaspace — метаданные классов.'),
    ('Stack — объекты, Heap — примитивы', 154, false, NULL),
    ('Оба хранят объекты, разница только в скорости', 154, false, NULL),
    ('Stack разделяется потоками, Heap — потокобезопасен', 154, false, NULL),

    ('Ошибка при переполнении стека вызовов — обычно глубокая/бесконечная рекурсия', 155, true,
        'Каждый вызов метода кладёт фрейм в stack; при превышении лимита глубины (по умолчанию ~512-1024KB) — StackOverflowError. Частая причина: рекурсия без условия выхода.'),
    ('Ошибка GC, не успевшего очистить стек', 155, false, NULL),
    ('Переполнение heap при нехватке памяти', 155, false, NULL),
    ('Ошибка при превышении лимита открытых файлов', 155, false, NULL),

    ('Young Generation (Eden + Survivor S0/S1) и Old Generation; плюс Metaspace отдельно', 156, true,
        'Объекты создаются в Eden, переживаемые Minor GC уходят в Survivor, долгоживущие — в Old (Promotion). Metaspace хранит метаданные классов вне heap.'),
    ('Только один непрерывный регион', 156, false, NULL),
    ('Code cache и JIT-буферы', 156, false, NULL),
    ('Stack-регионы по числу потоков', 156, false, NULL),

    ('Young — область короткоживущих объектов (Eden, Survivor); Old — долгоживущие, пережившие несколько GC', 157, true,
        'Большинство объектов умирают молодыми: Minor GC быстро чистит Young. Объекты, пережившие порог, повышаются (promotion) в Old — там Major/Full GC.'),
    ('Young — потоки, Old — данные', 157, false, NULL),
    ('Young — примитивы, Old — объекты', 157, false, NULL),
    ('Это разные имена одной области', 157, false, NULL),

    ('В heap (ссылка) — как часть объекта Class; static-переменные не в стеке', 158, true,
        'Static-поля принадлежат классу и живут в heap с java.lang.Class объектом (концептуально — с Java 8 в heap). Локальные static-ссылки видны всем потокам — нужна синхронизация.'),
    ('В стеке основного потока', 158, false, NULL),
    ('В metaspace рядом с байт-кодом', 158, false, NULL),
    ('В регистрах CPU', 158, false, NULL);

-- 159-162: GC Roots
INSERT INTO questions (title) VALUES
    ('Что такое GC Roots?'),
    ('Какие типы GC Roots существуют?'),
    ('Как GC определяет, что объект — мусор?'),
    ('Что такое reachable и unreachable объекты?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Точки, от которых GC начинает обход графа объектов: всё достижимое — живое', 159, true,
        'GC Roots — корневые ссылки (стеки потоков, static-поля и т.д.). Mark & Sweep: GC помечает всё, достижимое из корней, остальное — мусор.'),
    ('Корневой каталог файловой системы JVM', 159, false, NULL),
    ('Первые созданные объекты приложения', 159, false, NULL),
    ('Классы, зарегистрированные в metaspace', 159, false, NULL),

    ('Static-поля, локальные переменные в стеке активных потоков, активные потоки, JNI-ссылки', 160, true,
        'Классические GC Roots: локальные переменные и параметры методов в стеках, static-поля, ссылки из активных потоков, JNI (native) ссылки, объекты, заблокированные синхронизацией.'),
    ('Только static-поля классов', 160, false, NULL),
    ('Только объекты в Old Generation', 160, false, NULL),
    ('Все объекты, созданные до старта GC', 160, false, NULL),

    ('Tracing (достижимость): помечает всё, достижимое из GC Roots; недостижимое — мусор', 161, true,
        'Mark and Sweep: фаза mark обходит граф от корней, фаза sweep удаляет непомеченное. Подсчёт ссылок (reference counting) в HotSpot не используется из-за циклических ссылок.'),
    ('По счётчику ссылок: обнулён счётчик — удаляется', 161, false, NULL),
    ('По возрасту объекта: старые удаляются первыми', 161, false, NULL),
    ('GC удаляет объекты, к которым давно не было обращений', 161, false, NULL),

    ('Reachable — достижимы из GC Roots (живые); unreachable — недостижимы (мусор)', 162, true,
        'Объект может стать unreachable, даже если на него ссылается другой unreachable-объект: целые недостижимые кластеры удаляются (в т.ч. с циклическими ссылками).'),
    ('Reachable — в Young, unreachable — в Old', 162, false, NULL),
    ('Reachable — объекты с финализатором', 162, false, NULL),
    ('Reachable — объекты в L2-кеше', 162, false, NULL);

-- 163-167: Garbage Collectors
INSERT INTO questions (title) VALUES
    ('Какие GC бывают и в чём их отличия?'),
    ('Какой GC по умолчанию в Java 17?'),
    ('В чём преимущество G1 перед Parallel GC?'),
    ('Когда использовать ZGC?'),
    ('Что такое stop-the-world?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Serial (1 поток), Parallel (throughput), G1 (regions, default с Java 9), ZGC/Shenandoah (низкие паузы)', 163, true,
        'Serial — для маленьких приложений; Parallel — максимум throughput, паузы длиннее; G1 — баланс, делит heap на regions; ZGC и Shenandoah — паузы < 1-10ms для больших heap.'),
    ('Только один GC — Serial для всех случаев', 163, false, NULL),
    ('MarkSweep и MarkCompact', 163, false, NULL),
    ('GC в Java выбирается случайно при старте', 163, false, NULL),

    ('G1 GC', 164, true,
        'С Java 9 G1 — collector по умолчанию (в Java 8 был Parallel). ZGC/Shenandoah — опционально для low-latency.'),
    ('Serial GC', 164, false, NULL),
    ('Parallel GC', 164, false, NULL),
    ('ZGC', 164, false, NULL),

    ('G1 предсказуемее по паузам: работает по регионам инкрементально, можно задать цель паузы', 165, true,
        'Parallel максимизирует throughput, но паузы дольше на больших heap. G1 делит heap на регионы и собирает частями с целью -XX:MaxGCPauseMillis.'),
    ('G1 не требует stop-the-world вообще', 165, false, NULL),
    ('G1 использует меньше CPU', 165, false, NULL),
    ('G1 убирает необходимость в настройке heap', 165, false, NULL),

    ('При больших heap и требованиях ultra-low latency (паузы < 1-10ms)', 166, true,
        'ZGC — параллельный, почти всегда concurrent, паузы не зависят от размера heap. Подходит для сервисов с жёсткими SLO по задержке и heap в десятки ГБ.'),
    ('Для маленьких heap до 100MB', 166, false, NULL),
    ('Когда важен максимальный throughput, а не паузы', 166, false, NULL),
    ('В Java 8 — единственный доступный', 166, false, NULL),

    ('Пауза, когда все потоки приложения остановлены для работы GC', 167, true,
        'Во время stop-the-world приложение «заморожено» — в это время недоступны ответы API. Цель современных GC — минимизировать длительность и частоту STW-пауз.'),
    ('Полная остановка всех JVM в кластере', 167, false, NULL),
    ('Остановка записи в WAL на время сборки мусора', 167, false, NULL),
    ('Блокировка всех потоков на synchronized-мониторе', 167, false, NULL);

-- 168-171: ClassLoader
INSERT INTO questions (title) VALUES
    ('Как работает delegation model у ClassLoader?'),
    ('Какие ClassLoader бывают?'),
    ('В чём разница ClassNotFoundException и NoClassDefFoundError?'),
    ('Зачем нужен custom ClassLoader?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Запрос на класс делегируется родителю вверх; класс грузится только один раз (name + classLoader)', 168, true,
        'Application ClassLoader сначала просит родителя (Extension/Platform → Bootstrap); загрузка происходит на самом верхнем уровне, который может её выполнить. Гарантирует единообразие core-классов.'),
    ('ClassLoader грузит классы параллельно во все потоки', 168, false, NULL),
    ('Дочерние загрузчики переопределяют стандартные классы JDK', 168, false, NULL),
    ('Классы загружаются лениво снизу вверх без родительской иерархии', 168, false, NULL),

    ('Bootstrap, Platform (Extension), Application/System + кастомные', 169, true,
        'Bootstrap — ядро JDK; Platform/Extension — расширения; Application — classpath приложения; плюс кастомные (например, в Tomcat, Fat JAR JarLauncher).'),
    ('Local, Remote, Distributed', 169, false, NULL),
    ('Static и Dynamic', 169, false, NULL),
    ('Только Application ClassLoader', 169, false, NULL),

    ('ClassNotFoundException — checked exception при явной загрузке по имени; NoClassDefFoundError — Error при линковке, когда класс найден при компиляции, но нет в runtime', 170, true,
        'CNFE — from Class.forName()/loadClass(): класс не найден по имени. NCDFE — Error: класс был в classpath при компиляции, но отсутствует/битый при инициализации в runtime.'),
    ('Это синонимы для одной ситуации', 170, false, NULL),
    ('CNFE — Error, NCDFE — Exception', 170, false, NULL),
    ('Оба — checked exceptions', 170, false, NULL),

    ('Загрузка классов из нестандартных источников: сеть, шифрованные JAR, hot-reload, изоляция приложений', 170 + 1, true,
        'Custom ClassLoader переопределяет findClass(): грузит байткод откуда угодно. Используется в контейнерах (Tomcat — изоляция webapp), OSGi, инструментах hot-reload, плагинных системах.'),
    ('Чтобы ускорить загрузку классов кэшированием', 171, false, NULL),
    ('Только для отладки памяти', 171, false, NULL),
    ('Незачем: все загрузчики стандартные', 171, false, NULL);

-- 172-175: PermGen vs Metaspace
INSERT INTO questions (title) VALUES
    ('В чём разница PermGen и Metaspace?'),
    ('Почему убрали PermGen?'),
    ('Как диагностировать OOM?'),
    ('Что такое heap dump?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('PermGen — фиксированный размер в heap (Java 7); Metaspace — в native memory, растёт динамически (Java 8+)', 172, true,
        'PermGen имел ограниченный размер (OutOfMemoryError: PermGen space). Metaspace хранит метаданные классов в native memory и расширяется по необходимости (лимит MaxMetaspaceSize).'),
    ('PermGen в стеке, Metaspace в heap', 172, false, NULL),
    ('PermGen для объектов, Metaspace для примитивов', 172, false, NULL),
    ('Это разные названия одного региона', 172, false, NULL),

    ('Фиксированный размер вызывал OOM; интеграция с JRockit и упрощение GC', 173, true,
        'PermGen требовал ручной настройки размера и часто приводил к OutOfMemoryError при динамической генерации классов. Metaspace в native memory снял проблему, плюс унификация с JRockit.'),
    ('Метаданные классов стали слишком большими', 173, false, NULL),
    ('PermGen не поддерживал многопоточность', 173, false, NULL),
    ('PermGen замедлял загрузку классов', 173, false, NULL),

    ('Heap dump (jmap, VisualVM, MAT), анализ утечек, метрики, -XX:+HeapDumpOnOutOfMemoryError', 174, true,
        'Снять heap dump (jmap -dump, флаг HeapDumpOnOutOfMemoryError), открыть в Eclipse MAT / VisualVM: доминирующие деревья, утечки, retained size. Плюс GC-логи и метрики.'),
    ('Перезапустить приложение с большим heap — диагностика завершена', 174, false, NULL),
    ('OOM всегда означает утечку — искать только в коде', 174, false, NULL),
    ('Через профилировщик CPU: OOM — проблема процессора', 174, false, NULL),

    ('Снимок памяти heap в момент снятия: все объекты, классы, ссылки — для анализа утечек', 174 + 1, true,
        'Heap dump — файл формата HPROF с полной картиной heap. В MAT видно, какие объекты занимают память и кто их удерживает (path to GC roots).'),
    ('Дамп стека всех потоков (thread dump)', 175, false, NULL),
    ('Лог всех SQL-запросов', 175, false, NULL),
    ('Резервная копия БД приложения', 175, false, NULL);

-- ============================================================================
-- 5. MULTITHREADING
-- ============================================================================

-- 176-180: Thread Pool
INSERT INTO questions (title) VALUES
    ('Какие типы Thread Pool бывают?'),
    ('Что происходит, когда очередь пула заполнена, а потоки на максимуме?'),
    ('Какие RejectionPolicy бывают?'),
    ('Как выбрать размер Thread Pool?'),
    ('Почему не стоит использовать Executors.newCachedThreadPool?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Fixed, Cached, Scheduled, SingleThread, WorkStealing', 176, true,
        'Fixed — фиксированное число потоков; Cached — растёт/сжимается; Scheduled — отложенные и периодические задачи; SingleThread — один поток последовательно; WorkStealing — work-stealing (Java 8+).'),
    ('Static, Dynamic, Elastic', 176, false, NULL),
    ('Sync pool и Async pool', 176, false, NULL),
    ('Только ThreadPoolExecutor', 176, false, NULL),

    ('Задача передаётся RejectedExecutionHandler — по умолчанию AbortPolicy (бросает исключение)', 177, true,
        'Порядок: core-потоки → очередь → новые потоки до maxPoolSize → rejection policy. По умолчанию задача отклоняется с RejectedExecutionException.'),
    ('Задача молча теряется', 177, false, NULL),
    ('Пул автоматически увеличивает очередь', 177, false, NULL),
    ('Выполняющий поток блокируется до появления места', 177, false, NULL),

    ('Abort (exception), CallerRuns (выполняет вызывающий поток), Discard (молча), DiscardOldest (выкидывает старейшую)', 178, true,
        'AbortPolicy — default, исключение. CallerRunsPolicy — backpressure: задача выполняется в submit-потоке. DiscardPolicy — тихо теряет. DiscardOldest — выкидывает голову очереди.'),
    ('Retry, Failover, Timeout', 178, false, NULL),
    ('Block, Wait, Cancel', 178, false, NULL),
    ('Политик нет — всегда AbortPolicy', 178, false, NULL),

    ('CPU-bound: cores + 1; IO-bound: cores * 2+ (больше — потоки ждут IO)', 179, true,
        'CPU-bound задачи ограничены числом ядер (+1 для простоев). IO-bound могут ждать сеть/диск — потоков нужно больше, чтобы ядра не простаивали. Тюнить по замерам.'),
    ('Всегда максимально много потоков', 179, false, NULL),
    ('Всегда по числу ядер', 179, false, NULL),
    ('Размер не влияет на производительность', 179, false, NULL),

    ('Неограниченное число потоков (Integer.MAX_VALUE) — риск исчерпания ресурсов при всплесках нагрузки', 180, true,
        'CachedThreadPool создаёт поток на каждую задачу при отсутствии свободных: при пике нагрузки — тысячи потоков, OOM на stack. Для production — Fixed или ThreadPoolExecutor с настройками.'),
    ('Он однопоточный и не масштабируется', 180, false, NULL),
    ('Он не поддерживает Future', 180, false, NULL),
    ('Он синхронизирован и медленный', 180, false, NULL);

-- 181-184: CompletableFuture
INSERT INTO questions (title) VALUES
    ('Что такое CompletableFuture?'),
    ('В чём разница thenApply и thenCompose?'),
    ('Как обработать exception в CompletableFuture?'),
    ('Как дождаться завершения нескольких futures?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Реализация Future с колбэками: асинхронные цепочки, комбинирование, обработка ошибок', 181, true,
        'CompletableFuture расширяет Future: задачи через supplyAsync/runAsync, цепочки then*, объединение allOf/anyOf, обработка ошибок exceptionally/handle — без ручного blocking get().'),
    ('Улучшенный synchronized для асинхронного кода', 181, false, NULL),
    ('Пул потоков с приоритетами задач', 181, false, NULL),
    ('Аналог Stream API для коллекций', 181, false, NULL),

    ('thenApply — синхронное преобразование результата; thenCompose — возвращает новый CompletableFuture (flatMap)', 182, true,
        'thenApply: Function<T,R> — маппинг значения. thenCompose: Function<T, CompletableFuture<R>> — избегает вложенности CompletableFuture<CompletableFuture<R>>.'),
    ('thenApply асинхронный, thenCompose синхронный', 182, false, NULL),
    ('thenApply для ошибок, thenCompose для успеха', 182, false, NULL),
    ('Разницы нет, это алиасы', 182, false, NULL),

    ('exceptionally / handle / whenComplete — перехват и восстановление после исключения', 183, true,
        'exceptionally: альтернативное значение при ошибке. handle: работает и с результатом, и с ошибкой. whenComplete: side-effect без замены результата. Исключение из цепочки всплывает при get()/join().'),
    ('Только try-catch вокруг join()', 183, false, NULL),
    ('Через throws в сигнатуре метода', 183, false, NULL),
    ('CompletableFuture не бросает исключений', 183, false, NULL),

    ('CompletableFuture.allOf(...).join() — для всех; anyOf — для первого завершившегося', 184, true,
        'allOf возвращает CompletableFuture<Void>, завершающийся после всех; результат каждого забирается отдельно. anyOf завершается, как только завершится первый.'),
    ('Только последовательные join() в цикле', 184, false, NULL),
    ('Через Thread.join() для каждого', 184, false, NULL),
    ('Через synchronized на общем объекте', 184, false, NULL);

-- 185-187: ForkJoinPool
INSERT INTO questions (title) VALUES
    ('Что такое work-stealing?'),
    ('В чём разница RecursiveTask и RecursiveAction?'),
    ('Как parallel stream связан с ForkJoinPool?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Свободные потоки «крадут» задачи из очередей занятых — балансировка нагрузки', 185, true,
        'У каждого потока своя deque задач; простаивающий поток забирает задачи с хвоста чужой очереди. Эффективно для divide-and-conquer задач с неравномерной декомпозицией.'),
    ('Алгоритм GC для перераспределения памяти', 185, false, NULL),
    ('Перехват задач у других JVM в кластере', 185, false, NULL),
    ('Приоритетная очередь задач в ThreadPoolExecutor', 185, false, NULL),

    ('RecursiveTask<V> возвращает результат; RecursiveAction — без результата (void)', 186, true,
        'Оба — для divide-and-conquer в ForkJoinPool: compute() делит задачу, fork() порождает подзадачи, join() собирает. Task — с результатом, Action — side-effect без результата.'),
    ('RecursiveTask многопоточный, RecursiveAction — нет', 186, false, NULL),
    ('RecursiveAction может кидать исключения, Task — нет', 186, false, NULL),
    ('Это синонимы', 186, false, NULL),

    ('Parallel stream по умолчанию выполняется в общем ForkJoinPool.commonPool()', 187, true,
        'Параллельные операции стримов распараллеливаются через commonPool (потоков = cores - 1). Блокирующие задачи в parallel stream заедают общий пул — для тяжёлого IO лучше свой пул.'),
    ('Parallel stream создаёт собственный пул на каждый вызов', 187, false, NULL),
    ('Parallel stream использует ThreadPoolExecutor', 187, false, NULL),
    ('Parallel stream выполняется в main-потоке', 187, false, NULL);

-- 188-191: ReentrantLock vs synchronized
INSERT INTO questions (title) VALUES
    ('В чём разница synchronized и ReentrantLock?'),
    ('Что делает tryLock()?'),
    ('Что такое ReentrantReadWriteLock?'),
    ('Когда ReentrantLock лучше synchronized?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('synchronized — неявный монитор с одним режимом; ReentrantLock — явный: tryLock, lockInterruptibly, fairness', 188, true,
        'synchronized проще, release автоматический. ReentrantLock даёт неблокирующую попытку (tryLock с timeout), прерываемое ожидание, справедливую очередь (fair = true) и несколько Condition.'),
    ('ReentrantLock быстрее во всех сценариях', 188, false, NULL),
    ('synchronized допускает tryLock с таймаутом', 188, false, NULL),
    ('Это синонимы', 188, false, NULL),

    ('Пытается взять блокировку без блокировки навсегда: с таймаутом или сразу возвращает false', 189, true,
        'tryLock() — неблокирующая попытка: вернёт true/false немедленно. tryLock(timeout, unit) — ждёт до таймаута. Позволяет избежать deadlock: не дождались — отступили.'),
    ('Блокирует поток навсегда до получения лока', 189, false, NULL),
    ('Проверяет, не занят ли лок другим потоком, и в любом случае ждёт', 189, false, NULL),
    ('Устанавливает таймаут на все операции с локом', 189, false, NULL),

    ('Блокировка «чтение — параллельно, запись — эксклюзивно»: много читателей или один писатель', 190, true,
        'ReadLock могут держать много потоков одновременно (если нет писателя); writeLock — эксклюзивный. Выгоден при read-heavy нагрузке с изменяемым состоянием.'),
    ('Лок, который автоматически повышается от read к write', 190, false, NULL),
    ('Два независимых synchronized-монитора', 190, false, NULL),
    ('Лок с гарантией справедливой очереди', 190, false, NULL),

    ('Когда нужны tryLock с таймаутом, прерывание ожидания, fairness или несколько Condition', 191, true,
        'Эти возможности synchronized недоступны. В простых случаях synchronized предпочтительнее: проще и без риска забыть unlock.'),
    ('Всегда — ReentrantLock современнее', 191, false, NULL),
    ('Только для read-heavy сценариев', 191, false, NULL),
    ('Ниогда: synchronized всегда лучше', 191, false, NULL);

-- 192-194: Latch/Barrier/Semaphore
INSERT INTO questions (title) VALUES
    ('В чём разница CountDownLatch и CyclicBarrier?'),
    ('Что делает Semaphore?'),
    ('Приведите use case для CountDownLatch, CyclicBarrier и Semaphore');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('CountDownLatch — одноразовое ожидание N событий; CyclicBarrier — переиспользуемая точка синхронизации N потоков', 192, true,
        'Latch: главный поток ждёт, пока N задач посчитаются; после нуля счётчика не переиспользуется. Barrier: N потоков ждут друг друга в барьере, после пересечения барьер снова работает (+ action).'),
    ('Оба переиспользуемые барьеры', 192, false, NULL),
    ('Latch для потоков, Barrier для процессов', 192, false, NULL),
    ('Barrier ограничивает доступ к ресурсу, Latch — точка встречи', 192, false, NULL),

    ('Ограничивает число потоков, одновременно имеющих доступ к ресурсу (permits)', 193, true,
        'acquire() забирает permit (блокируется, если их нет), release() возвращает. Классика: ограничение параллельных соединений/запросов.'),
    ('Синхронизирует доступ по принципу «первым пришёл — первым обслужен» только для чтения', 193, false, NULL),
    ('Гарантирует взаимную исключительность как synchronized', 193, false, NULL),
    ('Считает количество выполненных операций', 193, false, NULL),

    ('Latch — старт после подготовки; Barrier — фазы вычислений группой потоков; Semaphore — лимит соединений', 194, true,
        'CountDownLatch: сервисы ждут инициализации зависимостей. CyclicBarrier: многопоточные вычисления по фазам (шаги матрицы). Semaphore: пул на N параллельных обращений к внешнему API.'),
    ('Все три используются только для ожидания старта потоков', 194, false, NULL),
    ('Semaphore для ожидания, остальные для лимитирования', 194, false, NULL),
    ('Use cases взаимозаменяемы без ограничений', 194, false, NULL);

-- ============================================================================
-- 6. TESTING
-- ============================================================================

-- 195-198: Unit vs Integration vs E2E
INSERT INTO questions (title) VALUES
    ('В чём разница unit, integration и E2E тестов?'),
    ('Что такое пирамида тестов?'),
    ('Когда писать integration вместо unit?'),
    ('Что такое test double?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Unit — класс в изоляции с моками; integration — взаимодействие компонентов (БД, контекст); E2E — весь flow HTTP → DB', 195, true,
        'Unit быстрые и точечные; integration проверяет связки (репозиторий + БД); E2E — полный пользовательский сценарий через HTTP.'),
    ('Unit и integration — синонимы, E2E — только UI-тесты', 195, false, NULL),
    ('Unit — фронтенд, integration — бэкенд, E2E — база данных', 195, false, NULL),
    ('Различие только в скорости выполнения', 195, false, NULL),

    ('Много unit, меньше integration, мало E2E — баланс скорости и уверенности', 196, true,
        'Пирамида: основание — быстрые дешёвые unit-тесты, середина — integration, вершина — немного медленных дорогих E2E. Антипаттерн — «мороженое» (много E2E).'),
    ('Больше всего E2E — они надёжнее', 196, false, NULL),
    ('Все уровни в равных пропорциях', 196, false, NULL),
    ('Только unit-тесты нужны', 196, false, NULL),

    ('Когда логика завязана на взаимодействие: репозитории, SQL, интеграции, где мок скроет реальные проблемы', 197, true,
        'Чистая бизнес-логика — unit. Проверка запросов, транзакций, миграций, dialect-особенностей БД — integration (@DataJpaTest, TestContainers).'),
    ('Всегда — integration надёжнее', 197, false, NULL),
    ('Только для тестирования контроллеров', 197, false, NULL),
    ('Integration пишут только для legacy-кода', 197, false, NULL),

    ('Общее название для замен реальных зависимостей: mock, stub, fake, spy, dummy', 198, true,
        'Dummy — заполняет параметр; Stub — возвращает фиксированные данные; Fake — рабочая упрощённая реализация (H2); Mock — проверяет взаимодействие; Spy — обёртка над реальным объектом с записью вызовов.'),
    ('Тестовый фреймворк для написания тестов', 198, false, NULL),
    ('Двойной прогон теста для стабильности', 198, false, NULL),
    ('Тест, проверяющий два модуля сразу', 198, false, NULL);

-- 199-202: Mockito
INSERT INTO questions (title) VALUES
    ('В чём разница @Mock и @Spy?'),
    ('Что делает @InjectMocks?'),
    ('В чём разница @Mock и @MockBean?'),
    ('Когда использовать Spy вместо Mock?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('@Mock — полностью фейковый объект (заглушки); @Spy — реальный объект с возможностью переопределить методы', 199, true,
        'У Mock все методы возвращают default без стабов. Spy оборачивает реальный объект: вызовы идут в реальную реализацию, если не заглушены (doReturn...when).'),
    ('@Mock реальный, @Spy фейковый', 199, false, NULL),
    ('@Mock для интерфейсов, @Spy для классов', 199, false, NULL),
    ('Это синонимы из разных версий Mockito', 199, false, NULL),

    ('Создаёт тестируемый объект и внедряет в него объявленные @Mock/@Spy зависимости', 200, true,
        '@InjectMocks инстанцирует класс и расставляет моки по полям/конструктору/setterам. Обычно в связке MockitoExtension + @Mock + @InjectMocks.'),
    ('Загружает Spring-контекст для теста', 200, false, NULL),
    ('Мокирует сам тестовый класс', 200, false, NULL),
    ('Создаёт моки автоматически без объявления', 200, false, NULL),

    ('@Mock — Mockito-объект в unit-тесте; @MockBean — заменяет бин в Spring-контексте', 201, true,
        '@MockBean (Spring Boot) регистрирует мок в контексте вместо существующего бина — для @SpringBootTest/@WebMvcTest. @MockBean устарел в Boot 3.4+, рекомендуется @MockitoBean.'),
    ('Это синонимы, работает одинаково везде', 201, false, NULL),
    ('@MockBean быстрее, т.к. без контекста', 201, false, NULL),
    ('@Mock работает только с интерфейсами', 201, false, NULL),

    ('Когда нужна реальная логика большинства методов, а заглушить надо лишь несколько', 202, true,
        'Spy частично реальный: например, мокировать только отправку email в реальном EmailService. Для полностью изолированного теста — Mock.'),
    ('Никогда — Spy только для legacy', 202, false, NULL),
    ('Всегда — Spy надёжнее', 202, false, NULL),
    ('Только для final классов', 202, false, NULL);

-- 203-205: Test Slices
INSERT INTO questions (title) VALUES
    ('В чём разница @SpringBootTest и @DataJpaTest?'),
    ('Какой slice использовать для тестирования REST контроллера?'),
    ('Почему @SpringBootTest медленный?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('@SpringBootTest грузит весь контекст; @DataJpaTest — только JPA-слой (DataSource, EntityManager, репозитории)', 203, true,
        'DataJpaTest — slice: быстрый, конфигурирует embedded-БД/TestContainers, откатывает транзакции по умолчанию. SpringBootTest — полный контекст, медленный, но интеграционный.'),
    ('Разницы нет — оба грузят всё приложение', 203, false, NULL),
    ('DataJpaTest для контроллеров, SpringBootTest для БД', 203, false, NULL),
    ('SpringBootTest быстрее, т.к. кешируется JVM', 203, false, NULL),

    ('@WebMvcTest — только web-слой: контроллеры, фильтры, MockMvc', 204, true,
        '@WebMvcTest загружает только MVC-инфраструктуру; сервисы внедряют через @MockBean/@MockitoBean. Тестируем маппинг, валидацию, JSON-сериализацию без БД.'),
    ('@DataJpaTest', 204, false, NULL),
    ('@JsonTest', 204, false, NULL),
    ('@SpringBootTest — только он тестирует HTTP', 204, false, NULL),

    ('Грузит и держит весь Spring-контекст: все бины, конфигурации, соединения', 205, true,
        'Поднимаются все бины приложения — секунды на старт. Смягчение: кеширование контекста между тестами (одинаковая конфигурация), слайсы, @DirtiesContext экономно.'),
    ('Он запускает настоящий сервер на каждый тест', 205, false, NULL),
    ('Он всегда дергает реальную внешнюю БД', 205, false, NULL),
    ('Это миф: SpringBootTest быстрый', 205, false, NULL);

-- 206-208: TestContainers
INSERT INTO questions (title) VALUES
    ('Зачем TestContainers, если есть H2?'),
    ('Какие проблемы решает TestContainers?'),
    ('Как настроить PostgreSQL TestContainer?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('H2 — не настоящая PostgreSQL: другие dialect, функции, поведение; TestContainers даёт реальную БД в Docker', 206, true,
        'H2 имитирует SQL, но расходится с PostgreSQL в типах, функциях, блокировках, isolation. TestContainers поднимает реальный postgres:17 — тесты ловят реальные dialect-проблемы.'),
    ('TestContainers быстрее H2 за счёт кеша', 206, false, NULL),
    ('TestContainers не требует Docker', 206, false, NULL),
    ('H2 устарела и не поддерживается', 206, false, NULL),

    ('Расхождение SQL-диалектов H2 и реальной БД, мокирование БД, «тесты зеленые локально, красные в проде»', 207, true,
        'Реальная БД в контейнере: те же типы, функции, isolation, миграции Flyway отрабатывают как в проде. Каждый тест — чистая БД, детерминизм.'),
    ('Ускоряет тесты за счёт многопоточности', 207, false, NULL),
    ('Убирает необходимость в mockito', 207, false, NULL),
    ('Позволяет тестировать фронтенд', 207, false, NULL),

    ('@Container PostgreSQLContainer + @DynamicPropertySource для spring.datasource.*', 208, true,
        '@Testcontainers + @Container static PostgreSQLContainer<>("postgres:17"); @DynamicPropertySource пробрасывает url/username/password в Spring-контекст. Или @ServiceConnection в Boot 3.1+.'),
    ('Скачать PostgreSQL локально и указать в application-test.yml', 208, false, NULL),
    ('Через аннотацию @DataJpaTest(postgres = true)', 208, false, NULL),
    ('TestContainers не требует настройки — работает автоматически', 208, false, NULL);

-- ============================================================================
-- 7. ARCHITECTURE
-- ============================================================================

-- 209-212: Monolith vs Microservices
INSERT INTO questions (title) VALUES
    ('Плюсы и минусы monolith?'),
    ('Плюсы и минусы microservices?'),
    ('Когда лучше остаться на monolith?'),
    ('Какие проблемы добавляет distributed system?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Плюсы: простой деплой/транзакции/разработка. Минусы: единый масштаб, одна точка отказа, рост кодовой базы', 209, true,
        'Монолит: один процесс — ACID-транзакции «бесплатно», простой дебаг и деплой. Проблемы: масштабировать всё целиком, отказ одной части валит всё, при росте команды — конфликты и долгие сборки.'),
    ('Только плюсы: проще во всём', 209, false, NULL),
    ('Монолит не поддерживает REST API', 209, false, NULL),
    ('Минус один: не работает с PostgreSQL', 209, false, NULL),

    ('Плюсы: независимый деплой/масштаб/технологии. Минусы: distributed-сложность, observability, консистентность', 210, true,
        'Микросервисы: каждая команда деплоит и масштабирует своё, изоляция отказов. Цена: сеть, распределённые транзакции, eventual consistency, инфраструктура, мониторинг множества сервисов.'),
    ('Плюсы: быстрее разработка за счёт разделения кода', 210, false, NULL),
    ('Минусов нет, если правильно спроектировать', 210, false, NULL),
    ('Минус: нельзя использовать ORM', 210, false, NULL),

    ('Малая команда, ранний продукт, нет чётких границ доменов и болей масштабирования', 211, true,
        'Стандартный совет: стартовать с модульного монолита, выделять сервисы по реальным потребностям (нагрузка, команды, независимые релизы), а не моде.'),
    ('Никогда — микросервисы всегда лучше', 211, false, NULL),
    ('Только если данных больше 1 ТБ', 211, false, NULL),
    ('Только при отсутствии интернета', 211, false, NULL),

    ('Сетевые сбои, распределённые транзакции, eventual consistency, сложная observability', 212, true,
        'Сеть ненадёжна: таймауты, ретраи, деградации. Нет единой ACID-транзакции → saga/compensation. Дебаг через логи/трейсинг нескольких сервисов.'),
    ('Только повышенное потребление памяти', 212, false, NULL),
    ('Проблем нет, если использовать Kafka', 212, false, NULL),
    ('Требуется обязательно Seriaizable', 212, false, NULL);

-- 213-216: REST vs GraphQL
INSERT INTO questions (title) VALUES
    ('В чём разница REST и GraphQL?'),
    ('Что такое over-fetching и under-fetching?'),
    ('Какие недостатки у GraphQL?'),
    ('Когда выбрать GraphQL вместо REST?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('REST — множество эндпоинтов с фиксированными ответами; GraphQL — один эндпоинт, клиент запрашивает нужные поля', 213, true,
        'REST: ресурсы и HTTP-методы, структура ответа фиксирована. GraphQL: query описывает ровно нужные поля — решает over/under-fetching, но серверу нужен resolver-слой.'),
    ('GraphQL для запросов, REST для мутаций', 213, false, NULL),
    ('REST медленнее, т.к. не кешируется', 213, false, NULL),
    ('Это синонимы', 213, false, NULL),

    ('Over — клиент получает лишние поля; under — нужно несколько запросов, чтобы собрать данные', 214, true,
        'Over-fetching: эндпоинт /users возвращает 30 полей, нужно 2. Under-fetching: страница собирается из /user + /orders + /settings — несколько round-trip. GraphQL решает оба.'),
    ('Ошибки сети при передаче больших файлов', 214, false, NULL),
    ('Термин кеша: переполнение и вытеснение', 214, false, NULL),
    ('Ошибки валидации на клиенте и сервере', 214, false, NULL),

    ('Сложность кеширования, N+1 в резолверах, безопасность запросов, порог входа', 215, true,
        'HTTP-кеш плохо работает (всё POST на один URL), резолверы легко порождают N+1, глубина запросов требует лимитов, авторизация по полям сложнее.'),
    ('Не поддерживает мутации данных', 215, false, NULL),
    ('Работает только с NoSQL', 215, false, NULL),
    ('Не умеет возвращать ошибки', 215, false, NULL),

    ('Когда клиенты сильно различаются по нужным данным и важна экономия round-trip (мобильные, агрегация нескольких сервисов)', 216, true,
        'GraphQL выгоден для гибких клиентов и BFF-агрегации. Для простых CRUD-API REST проще и стандартнее.'),
    ('Всегда — GraphQL современнее', 216, false, NULL),
    ('Только для read-only API', 216, false, NULL),
    ('Когда нужна максимальная скорость сети', 216, false, NULL);

-- 217-220: Kafka vs RabbitMQ
INSERT INTO questions (title) VALUES
    ('В чём разница Kafka и RabbitMQ?'),
    ('Что такое partition в Kafka?'),
    ('Когда выбрать Kafka?'),
    ('Когда выбрать RabbitMQ?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Kafka — распределённый лог (pull, partitioning, retention); RabbitMQ — брокер очередей (push, routing, exchanges)', 217, true,
        'Kafka: события пишутся в append-only лог, консьюмеры сами читают с offset, сообщения хранятся долго. RabbitMQ: push-модель, сложные маршрутизации через exchanges, сообщения удаляются после ack.'),
    ('Это синонимы для message queue', 217, false, NULL),
    ('Kafka для маленьких систем, RabbitMQ для больших', 217, false, NULL),
    ('RabbitMQ заменяет базу данных', 217, false, NULL),

    ('Сегмент лога топика; порядок сообщений гарантирован внутри одного partition', 218, true,
        'Топик делится на partitions — единицы параллелизма и порядка. Сообщения с одним ключом идут в один partition (порядок сохранён); между partitions порядка нет. Консьюмер-группы распределяют partitions.'),
    ('Резервная копия топика', 218, false, NULL),
    ('Единица кластера — сервер Kafka', 218, false, NULL),
    ('Буфер для ретраев неудачных сообщений', 218, false, NULL),

    ('Event streaming с высоким throughput, event sourcing, log aggregation, replay событий', 219, true,
        'Kafka — когда события — первичные данные: миллионы сообщений в секунду, plusieurs консьюмеров читают один поток, повторное чтение истории (replay), аналитика.'),
    ('Для сложной маршрутизации задач с приоритетами', 219, false, NULL),
    ('Для request-reply паттернов', 219, false, NULL),
    ('Когда объём сообщений мал и нужна простота', 219, false, NULL),

    ('Task queues, request-reply, сложная маршрутизация (exchanges, приоритеты, TTL)', 220, true,
        'RabbitMQ — гибкая маршрутизация (direct/topic/fanout exchanges), приоритетные очереди, dead-letter, RPC-паттерн. Отличный выбор для фоновых задач и интеграций.'),
    ('Для event sourcing с replay истории', 220, false, NULL),
    ('Когда нужен throughput миллионы сообщений/сек', 220, false, NULL),
    ('Для стриминга данных в аналитику', 220, false, NULL);

-- 221-224: Circuit Breaker
INSERT INTO questions (title) VALUES
    ('Объясните паттерн Circuit Breaker'),
    ('Какие состояния у Circuit Breaker?'),
    ('Что такое fallback?'),
    ('Какая библиотека Circuit Breaker используется в Spring?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Паттерн защиты от каскадных сбоев: после N неудач вызовы внешнего сервиса блокируются на время', 221, true,
        'Как электрический автомат: при серии ошибок «рубильник» размыкается — вызовы не идут, вместо этого fallback. Даёт сбойному сервису время восстановиться и защищает от лавины таймаутов.'),
    ('Механизм ретраев с экспоненциальной задержкой', 221, false, NULL),
    ('Балансировщик нагрузки между инстансами', 221, false, NULL),
    ('Способ кеширования ответов внешних API', 221, false, NULL),

    ('Closed → Open → Half-Open', 222, true,
        'Closed — запросы идут, ошибки считаются. Open — порог превышен, вызовы блокируются, отдаётся fallback. Half-Open — после таймаута пропускает пробные запросы: успех → Closed, неудача → Open.'),
    ('Start, Run, Stop', 222, false, NULL),
    ('Read, Write, Commit', 222, false, NULL),
    ('Active, Passive, Idle', 222, false, NULL),

    ('Запасной ответ/действие при недоступности сервиса: кеш, дефолт, деградация функциональности', 223, true,
        'Fallback вызывается вместо реального запроса (Open state) или при ошибке: вернуть кешированные данные, дефолт, пустой результат — сервис продолжает работать с деградацией.'),
    ('Откат транзакции в БД', 223, false, NULL),
    ('Повторный запрос к сервису', 223, false, NULL),
    ('Лог ошибки в системе мониторинга', 223, false, NULL),

    ('Resilience4j (Resilience4j CircuitBreaker, spring-cloud-circuitbreaker)', 224, true,
        'Resilience4j — современная замена Hystrix (устарел): CircuitBreaker, Retry, RateLimiter, Bulkhead, аннотация @CircuitBreaker(name = "...", fallbackMethod = "...").'),
    ('Netflix Hystrix', 224, false, NULL),
    ('Spring Retry', 224, false, NULL),
    ('Apache Camel', 224, false, NULL);

-- ============================================================================
-- 8. DATABASES & SQL
-- ============================================================================

-- 225-228: SQL Injection
INSERT INTO questions (title) VALUES
    ('Объясните SQL Injection на примере'),
    ('Какая основная защита от SQL Injection?'),
    ('Почему валидации недостаточно как защита от SQL Injection?'),
    ('Защищает ли Hibernate от SQL Injection?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Внедрение произвольного SQL через ввод: конкатенация "''" + username позволяет выполнить чужой запрос', 225, true,
        '"SELECT * FROM users WHERE name = ''" + input + "''" — ввод '' OR 1=1 -- меняет логику запроса: возвращает всех пользователей или выполняет UNION/DELETE.'),
    ('Переполнение буфера через длинные SQL-запросы', 225, false, NULL),
    ('Кража JWT-токена через SQL-запрос', 225, false, NULL),
    ('Ошибка синтаксиса SQL из-за локали', 225, false, NULL),

    ('Prepared Statements (parameterized queries): данные идут как параметры, не как часть SQL', 226, true,
        '"... WHERE username = ?" с bind-параметром: значение никогда не интерпретируется как SQL. ORM (Hibernate/JPA) используют параметризацию по умолчанию.'),
    ('Экранирование кавычек вручную', 226, false, NULL),
    ('Хранение SQL в отдельных файлах', 226, false, NULL),
    ('Ограничение длины ввода', 226, false, NULL),

    ('Валидация — доп. слой, но не защита: новые векторы и эскейп-последовательности обходят чёрные списки', 227, true,
        'Чёрные списки и regex неполны: обходные кодировки, second-order injection (данные из БД). Единственная надёжная защита — параметризованные запросы.'),
    ('Валидация блокирует все атаки, но работает медленно', 227, false, NULL),
    ('Валидация достаточна, если экранировать кавычки', 227, false, NULL),
    ('Валидация защищает, только если БД PostgreSQL', 227, false, NULL),

    ('Да, по умолчанию: параметры экранируются; уязвимы только нативные запросы с конкатенацией', 228, true,
        'JPQL-параметры (:name) биндятся безопасно. Опасность — nativeQuery с ручной конкатенацией строк: там тоже надо использовать параметры.'),
    ('Нет, Hibernate уязвим всегда', 228, false, NULL),
    ('Только с включённым show-sql', 228, false, NULL),
    ('Только если используется H2', 228, false, NULL);

-- 229-234: Schema, DDL, Flyway
INSERT INTO questions (title) VALUES
    ('Что такое schema в БД?'),
    ('В чём разница DDL и DML?'),
    ('Для чего нужен Flyway?'),
    ('Как именуются миграции Flyway?'),
    ('Зачем нужен baseline-on-migrate?'),
    ('Почему ddl-auto = validate вместе с Flyway?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Структура БД: таблицы, колонки, типы, связи, индексы — не сами данные', 229, true,
        'Schema — «каркас»: определения объектов и их связи. Данные (строки) — содержание. Термин также означает именованное пространство объектов внутри БД.'),
    ('Список всех таблиц с их строками', 229, false, NULL),
    ('Пароль пользователя БД', 229, false, NULL),
    ('Схема соединения драйвера с сервером', 229, false, NULL),

    ('DDL управляет схемой (CREATE, ALTER, DROP); DML — данными (INSERT, UPDATE, DELETE, SELECT)', 230, true,
        'Data Definition Language меняет структуру объектов; Data Manipulation Language — содержимое таблиц. Разные категории также: DCL (GRANT/REVOKE), TCL (COMMIT/ROLLBACK).'),
    ('DDL для чтения, DML для записи', 230, false, NULL),
    ('DDL — Java API, DML — SQL-команды', 230, false, NULL),
    ('Это синонимы', 230, false, NULL),

    ('Версионированные миграции схемы: команды выполняются в порядке и фиксируются в flyway_schema_history', 231, true,
        'Flyway применяет SQL-миграции последовательно, отслеживает применённые в таблице history — вся команда и все окружения получают одинаковую схему.'),
    ('Генерация CRUD-кода из таблиц', 231, false, NULL),
    ('Резервное копирование БД', 231, false, NULL),
    ('Оптимизация SQL-запросов', 231, false, NULL),

    ('V<номер>__описание.sql, например V1__init.sql, V2__initial_filling.sql', 232, true,
        'Версия — целое число (или через точки 1.2.3), два подчёркивания, описание. Flyway применяет в порядке версий и refuses пропуски при конфликте history.'),
    ('migration_1.sql, migration_2.sql', 232, false, NULL),
    ('Через дату: 2026-01-15_add_users.sql', 232, false, NULL),
    ('Имя произвольное, порядок по дате файла', 232, false, NULL),

    ('Пометить существующую БД как baseline: миграции до baseline-версии пропускаются', 233, true,
        'Для БД, созданной до внедрения Flyway: baseline-on-migrate = true ставит отметку baseline, и старые версии V1... не применяются повторно к готовой схеме.'),
    ('Создаёт резервную копию перед миграцией', 233, false, NULL),
    ('Откатывает последнюю миграцию при ошибке', 233, false, NULL),
    ('Автоматически генерирует миграции из сущностей', 233, false, NULL),

    ('Flyway управляет схемой, Hibernate только проверяет соответствие сущностей — никто не «чинит» схему молча', 234, true,
        'ddl-auto = update опасен (неконтролируемые изменения), create — стирает данные. validate гарантирует, что сущности соответствуют схеме из миграций, но не меняет её.'),
    ('Hibernate генерирует схему быстрее, чем Flyway', 234, false, NULL),
    ('validate ускоряет старт приложения', 234, false, NULL),
    ('ddl-auto = create безопаснее для данных', 234, false, NULL);

-- 235-241: Indexes
INSERT INTO questions (title) VALUES
    ('Как работает B-Tree index?'),
    ('Когда index НЕ сработает?'),
    ('Что такое composite index и почему порядок колонок важен?'),
    ('Что такое covering index?'),
    ('Какие минусы у индексов?'),
    ('Как проверить, используется ли индекс?'),
    ('В чём разница Index Scan и Seq Scan?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Сбалансированное дерево: поиск, диапазоны и сортировка за O(log n) вместо полного скана O(n)', 235, true,
        'B-Tree — дефолтный индекс PostgreSQL: эффективен для =, <, >, BETWEEN, ORDER BY, LIKE ''prefix%''. Каждый уровень дерева сужает диапазон страниц БД.'),
    ('Индекс хранит только ключи и rowid, данные читаются отдельно', 235, false, NULL),
    ('Хэш-таблица в памяти процесса', 235, false, NULL),
    ('Копия таблицы, отсортированная по колонке', 235, false, NULL),

    ('Функция на колонке, LIKE ''%suffix'', != / NOT IN, OR по разным колонкам, низкая селективность', 236, true,
        'Индекс не используется, когда условие не может пройти по дереву: UPPER(col), LIKE с ведущим %, отрицания, OR (иногда BitmapOr). Также планировщик может выбрать Seq Scan при малой таблице или низкой селективности.'),
    ('При SELECT с LIMIT', 236, false, NULL),
    ('При JOIN по индексированной колонке', 236, false, NULL),
    ('Индекс работает всегда', 236, false, NULL),

    ('Индекс по нескольким колонкам; порядок определяет, для каких запросов он применим (левый префикс)', 237, true,
        'Composite (a, b) работает по a, по (a, b), но НЕ по b одному. Правило: сначала колонка с высокой селективностью / используемая в =, потом диапазонные.'),
    ('Порядок влияет только на размер индекса', 237, false, NULL),
    ('PostgreSQL сам переставляет колонки под запрос', 237, false, NULL),
    ('Композитный индекс равен нескольким одинарным', 237, false, NULL),

    ('Индекс содержит все нужные запросу колонки — чтение только из индекса, без обращения к таблице', 238, true,
        'Index-Only Scan: SELECT col1 WHERE col2 = x по индексу (col2, col1) не трогает heap-строки. Экономит random I/O; в PG требует актуального visibility map.'),
    ('Индекс, который не занимает место на диске', 238, false, NULL),
    ('Индекс поверх представления (view)', 238, false, NULL),
    ('Уникальный индекс, покрывающий PK', 238, false, NULL),

    ('Замедляют INSERT/UPDATE/DELETE, занимают место, требуют обслуживания', 239, true,
        'Каждая запись обновляет все индексы таблицы; индексы растут на диске; возможен index bloat. Индексировать только реально используемые колонки (FK — кандидаты).'),
    ('Индексы замедляют SELECT', 239, false, NULL),
    ('Минусов нет при правильной настройке', 239, false, NULL),
    ('Индексы блокируют параллельные запросы', 239, false, NULL),

    ('EXPLAIN ANALYZE: показывает план и фактическое выполнение (Index Scan vs Seq Scan)', 240, true,
        'EXPLAIN — план без выполнения; EXPLAIN ANALYZE — с реальными cost/rows/time. Index Scan — индекс используется; Seq Scan — полный перебор (не всегда плохо на маленьких таблицах).'),
    ('Через лог PostgreSQL log_statement', 240, false, NULL),
    ('Через SELECT COUNT(*) — чем быстрее, тем лучше индекс', 240, false, NULL),
    ('Никак, это внутреннее дело планировщика', 240, false, NULL),

    ('Index Scan — поиск через индекс точечных строк; Seq Scan — последовательное чтение всей таблицы', 241, true,
        'Seq Scan эффективен, когда нужно много строк (низкая селективность) или таблица маленькая. Index Scan — когда возвращается малая доля строк. Bitmap Scan — промежуточный вариант.'),
    ('Index Scan читает таблицу целиком, Seq Scan — по индексу', 241, false, NULL),
    ('Это синонимы', 241, false, NULL),
    ('Seq Scan всегда медленнее', 241, false, NULL);

-- 242-249: ACID
INSERT INTO questions (title) VALUES
    ('Расшифруйте ACID'),
    ('Как Atomicity реализуется в БД?'),
    ('Как Durability реализуется в PostgreSQL?'),
    ('Чем Consistency в ACID отличается от Consistency в CAP?'),
    ('Какое свойство из A/I/D обеспечивает undo log, а какое — WAL?'),
    ('За какую букву ACID отвечает приложение, а не только БД?'),
    ('Что произойдёт при DELETE пользователя с заказами, если FK без ON DELETE?'),
    ('В чём разница триггеров BEFORE и AFTER?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('Atomicity, Consistency, Isolation, Durability', 242, true,
        'A — всё или ничего (rollback). C — из валидного состояния в валидное (constraints + инварианты). I — параллельные транзакции не мешают. D — закоммиченное переживает сбои.'),
    ('Atomic, Consistent, Independent, Durable', 242, false, NULL),
    ('Access, Control, Index, Data', 242, false, NULL),
    ('Aggregate, Commit, Isolate, Deliver', 242, false, NULL),

    ('Undo log: журнал старых значений, по которому незавершённая транзакция откатывается', 243, true,
        'Перед изменением старое значение пишется в undo; при rollback изменения откатываются по нему. Atomicity — про ОДНУ транзакцию, в отличие от Isolation.'),
    ('Через WAL: все изменения пишутся в журнал до данных', 243, false, NULL),
    ('Через блокировки строк на время транзакции', 243, false, NULL),
    ('ЧерезMVCC-снимки', 243, false, NULL),

    ('WAL + fsync: изменения сначала в журнал на диск, потом в data files; восстановление из WAL при старте', 244, true,
        '«Сначала журнал»: COMMIT гарантирует запись WAL на диск (fsync). Сгорела страница в RAM — восстановим из WAL. Репликация — durability в расширенном смысле, но не базовый механизм ACID.'),
    ('Репликация на 3 узла перед COMMIT', 244, false, NULL),
    ('Undo log: откат незавершённых изменений', 244, false, NULL),
    ('Ежедневный backup базы', 244, false, NULL),

    ('ACID C — целостность (constraints/инварианты); CAP C — синхронность реплик в распределённой системе', 245, true,
        'ACID Consistency: транзакция не нарушает constraints и бизнес-правила. CAP Consistency: все узлы видят одни данные (линейная согласованность). Разные понятия с одним словом.'),
    ('Это одно и то же свойство', 245, false, NULL),
    ('CAP C про транзакции, ACID C про репликацию', 245, false, NULL),
    ('CAP C отменяет ACID при сетевых сбоях', 245, false, NULL),

    ('Undo log — Atomicity; WAL — Durability', 246, true,
        'Undo log хранит старые значения для отката незавершённой транзакции (A). WAL с fsync гарантирует сохранность закоммиченных данных при сбое (D).'),
    ('Undo log — Durability; WAL — Atomicity', 246, false, NULL),
    ('Оба отвечают за Isolation', 246, false, NULL),
    ('Undo log — Consistency; WAL — Atomicity', 246, false, NULL),

    ('Consistency (C): constraints держит БД, но бизнес-инварианты проверяет приложение', 247, true,
        'A/I/D — гарантии движка БД. C — контракт приложение↔БД: PK/FK/UNIQUE/CHECK в схеме плюс бизнес-правила (нельзя списать больше баланса) в service-слое.'),
    ('Atomicity: приложение решает, что откатывать', 247, false, NULL),
    ('Durability: приложение управляет fsync', 247, false, NULL),
    ('Isolation: приложение выставляет блокировки вручную', 247, false, NULL),

    ('DELETE будет отклонён: FK по умолчанию NO ACTION (RESTRICT) — нельзя удалить при зависимостях', 248, true,
        'Без ON DELETE FK запрещает удаление родителя с существующими детьми (ошибка constraint violation). Варианты: ON DELETE CASCADE (удалить заказы), SET NULL, RESTRICT.'),
    ('Заказы удалятся автоматически', 248, false, NULL),
    ('У заказов обнулится FK (SET NULL по умолчанию)', 248, false, NULL),
    ('Пользователь удалится, заказы останутся «осиротевшими»', 248, false, NULL),

    ('BEFORE выполняется до операции и может её отменить/изменить; AFTER — после, отменить не может', 249, true,
        'BEFORE-триггер (row/statement level) может вернуть NULL и отменить операцию или подправить NEW. AFTER — для аудита и каскадных действий по уже свершившемуся факту. Пишутся на PL/pgSQL.'),
    ('BEFORE для SELECT, AFTER для DML', 249, false, NULL),
    ('BEFORE асинхронный, AFTER синхронный', 249, false, NULL),
    ('AFTER может отменить операцию откатом всей транзакции', 249, false, NULL);

-- 250-252: Joins
INSERT INTO questions (title) VALUES
    ('В чём разница INNER JOIN и LEFT JOIN?'),
    ('Когда использовать LEFT JOIN?'),
    ('Что вернёт FULL OUTER JOIN, если у одной из сторон нет совпадений?');

INSERT INTO answers (text, question_id, is_correct, description) VALUES
    ('INNER — только совпадения из обеих таблиц; LEFT — все строки левой + NULLы там, где нет совпадений справа', 250, true,
        'INNER JOIN отбрасывает строки без пары. LEFT JOIN сохраняет все строки левой таблицы, подставляя NULL вместо отсутствующих правых. RIGHT/FULL — симметричные варианты.'),
    ('INNER — для больших таблиц, LEFT — для маленьких', 250, false, NULL),
    ('LEFT JOIN всегда быстрее', 250, false, NULL),
    ('INNER читает по индексу, LEFT — полностью таблицу', 250, false, NULL),

    ('Когда нужны ВСЕ строки левой таблицы, включая без пары: «все пользователи и их заказы, даже без заказов»', 251, true,
        'Классика: список сущностей с опциональной связью (LEFT JOIN + COUNT для «заказов нет»). Также для поиска «осиротевших» строк: LEFT JOIN ... WHERE right.id IS NULL.'),
    ('Только когда обе таблицы одинакового размера', 251, false, NULL),
    ('Когда нужен декартов продукт', 251, false, NULL),
    ('LEFT JOIN не нужен — всегда INNER', 251, false, NULL),

    ('Все строки обеих сторон; отсутствующие с обеих сторон дополнятся NULLами', 252, true,
        'FULL OUTER JOIN = LEFT + RIGHT: строки без пары из любой стороны сохраняются с NULLами на месте отсутствующей стороны.'),
    ('Только совпадающие строки', 252, false, NULL),
    ('Строки без совпадений будут отброшены с предупреждением', 252, false, NULL),
    ('Запрос вернёт ошибку: FULL OUTER требует совпадений', 252, false, NULL);

-- Категории для вопросов из interview-prep.md (диапазоны id соответствуют блокам вставки выше)
UPDATE questions SET category = 'Core Java'         WHERE id BETWEEN 11 AND 42;
UPDATE questions SET category = 'Spring'            WHERE id BETWEEN 43 AND 110;
UPDATE questions SET category = 'Data & JPA'        WHERE id BETWEEN 111 AND 153;
UPDATE questions SET category = 'JVM'               WHERE id BETWEEN 154 AND 175;
UPDATE questions SET category = 'Multithreading'    WHERE id BETWEEN 176 AND 194;
UPDATE questions SET category = 'Testing'           WHERE id BETWEEN 195 AND 208;
UPDATE questions SET category = 'Architecture'      WHERE id BETWEEN 209 AND 224;
UPDATE questions SET category = 'Databases & SQL'   WHERE id BETWEEN 225 AND 252;

-- Все вопросы категоризированы
ALTER TABLE questions ALTER COLUMN category SET NOT NULL;
