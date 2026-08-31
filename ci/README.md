# CI/CD Configuration

Эта директория содержит конфигурационные файлы для различных окружений и CI/CD пайплайнов.

## Структура

```
ci/
├── README.md                    # Этот файл
├── docker-compose.yml          # Оркестрация Docker сервисов
├── application-docker.yml       # Конфигурация Spring для Docker окружения
├── application-local.yml        # Конфигурация для локальной разработки (будет добавлен)
├── application-prod.yml        # Конфигурация для продакшена (будет добавлен)
└── docker/                      # Docker специфичные файлы (если понадобятся)
```

## Файлы конфигурации

### docker-compose.yml
Оркестрация всех сервисов приложения: PostgreSQL, Redis, Spring Boot app.

**Особенности:**
- Multi-service архитектура
- Health checks для всех сервисов
- Proper dependency management
- Network isolation
- Volume management для персистентности

**Использование:**
```bash
# Через Makefile (рекомендуется)
make up
make down
make logs

# Напрямую
docker-compose -f ci/docker-compose.yml up -d
docker-compose -f ci/docker-compose.yml down
docker-compose -f ci/docker-compose.yml logs -f
```

### application-docker.yml
Конфигурация Spring Boot для запуска в Docker контейнерах.

**Особенности:**
- Подключение к Docker сервисам (postgres, redis)
- Активация health checks
- Конфигурация логирования для контейнеризованной среды
- Производительные настройки пулов соединений

**Монтрирование в контейнере:**
```yaml
volumes:
  - ./ci/application-docker.yml:/app/config/application-docker.yml:ro
```

**Переменные окружения** (переопределяют значения в yml):
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `SPRING_REDIS_HOST`, `SPRING_REDIS_PORT`
- `JWT_SECRET`, `JWT_EXPIRATION`
- `JAVA_OPTS`

## Профили Spring

Приложение поддерживает несколько профилей для разных окружений:

| Профиль | Файл конфигурации | Назначение |
|--------|-------------------|------------|
| `docker` | `application-docker.yml` | Docker окружение |
| `local` | `application-local.yml` | Локальная разработка |
| `prod` | `application-prod.yml` | Продакшн |

## Безопасность

⚠️ **ВАЖНО**: Никогда не коммитьте секреты и пароли в эти файлы!
Используйте переменные окружения или secret management системы.

Для локальной разработки создайте `.env` файл (см. `.env.example` в корне проекта).
