# InterviewBeaterService

Приложение для подготовки к Java-собеседованиям (позиция middle/middle+).

## 📁 Структура проекта

```
interviewBeaterService/
├── ci/                               # CI/CD и инфраструктура
│   ├── README.md                     # Документация CI
│   ├── docker-compose.yml           # Оркестрация сервисов
│   └── application-docker.yml       # Spring конфигурация для Docker
├── src/                              # Исходный код (package-by-feature)
├── Dockerfile                        # x86_64 Docker образ (multi-stage)
├── Makefile                          # Удобные команды
├── .env.example                      # Шаблон переменных окружения
└── pom.xml                           # Maven конфигурация (будет создан)
```

**Ключевые директории:**
- `ci/` - вся инфраструктура в одном месте (Docker, конфигурации окружений)
- `src/` - чистый исходный код приложения
- Корневые файлы - сборка и утилиты

## 🐳 Docker Quick Start

### Требования
- Docker Engine 20.10+
- Docker Compose 2.0+

### Быстрый запуск

```bash
# Клонировать репозиторий
git clone <repository-url>
cd interviewBeaterService

# Собрать и запустить
make up
# или напрямую:
docker-compose -f ci/docker-compose.yml up -d

# Проверить здоровье сервиса
curl http://localhost:8080/actuator/health
```

### Доступные команды

```bash
make build          # Собрать Docker-образы
make up             # Запустить все сервисы
make down           # Остановить все сервисы
make restart        # Перезапустить сервисы
make logs           # Посмотреть логи всех сервисов
make logs app       # Посмотреть логи только приложения
make ps             # Показать работающие контейнеры
make clean          # Удалить всё (контейнеры, volumes, образы)
```

### Доступные сервисы

- **Приложение**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health
- **Prometheus Metrics**: http://localhost:8080/actuator/prometheus
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

### Архитектура Docker

```
┌─────────────────┐
│   interviewbeater-app   │
│  (Spring Boot 4.1)│
│   Java 21        │
└────────┬─────────┘
         │
         ├──────────────┐
         │              │
         ▼              ▼
┌─────────────────┐  ┌─────────────────┐
│    postgres     │  │      redis      │
│   PostgreSQL 17 │  │   Redis 7       │
└─────────────────┘  └─────────────────┘
```

### Варианты сборки

#### 1. Стандартная сборка (multi-stage)
```bash
docker-compose -f ci/docker-compose.yml build
```
Использует multi-stage build для минимизации размера образа.

#### 2. Сборка для x86_64 на других архитектурах
```bash
docker buildx build --platform linux/amd64 -t interviewbeater:latest .
```

#### 3. Локальная сборка JAR
```bash
make build-local
```

### Мониторинг

```bash
# Логи всех сервисов
docker-compose -f ci/docker-compose.yml logs -f

# Логи конкретного сервиса
docker-compose -f ci/docker-compose.yml logs -f app
docker-compose -f ci/docker-compose.yml logs -f postgres

# Статус контейнеров
docker-compose -f ci/docker-compose.yml ps

# Использование ресурсов
docker stats
```

### Troubleshooting

#### Проблемы с памятью
Если возникают проблемы с памятью, увеличьте лимит Docker:
```bash
docker-compose -f ci/docker-compose.yml down
# Увеличьте memory в ci/docker-compose.yml
docker-compose -f ci/docker-compose.yml up -d
```

#### Пересоздание БД
```bash
make down
docker volume rm interviewbeaterservice_postgres_data
make up
```

#### Проверка здоровья сервисов
```bash
curl http://localhost:8080/actuator/health
docker-compose -f ci/docker-compose.yml exec postgres pg_isready -U interviewbeater
docker-compose -f ci/docker-compose.yml exec redis redis-cli ping
```

### Продакшн-режим

Для продакшн-использования:
1. Измените пароли в `.env`
2. Настройте `JWT_SECRET`
3. Установите правильные `resource limits`
4. Настройте внешние volumes для персистентности
5. Включите HTTPS/SSL

## 📁 Структура проекта

```
interviewBeaterService/
├── ci/                          # CI/CD конфигурации
│   ├── README.md                # Документация CI
│   └── application-docker.yml  # Spring конфигурация для Docker
├── src/                         # Исходный код
├── Dockerfile                   # x86_64 Docker образ (multi-stage)
├── docker-compose.yml          # Оркестрация сервисов
├── Makefile                     # Удобные команды
├── .env.example                 # Шаблон переменных окружения
└── pom.xml                      # Maven конфигурация (будет создан)
```

**Ключевые директории:**
- `ci/` - конфигурации для разных окружений (Docker, local, prod)
- `src/` - исходный код приложения (package-by-feature)
- Корневые файлы - Docker инфраструктура и сборка

## 📝 Дополнительная информация

Подробная документация по проекту находится в файле:
- [2026-08-25-interviewbeater-design.md](2026-08-25-interviewbeater-design.md)

## 🔧 Разработка

### Локальная разработка без Docker

```bash
# Установите PostgreSQL и Redis локально
# Затем запустите:
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Тесты

```bash
# Unit тесты
./mvnw test

# Интеграционные тесты с Testcontainers
./mvnw verify
```

## 📄 Лицензия

[Добавьте информацию о лицензии]
