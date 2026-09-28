# Frontend Auth Design — InterviewBeaterService

**Дата:** 2026-09-29
**Статус:** Draft → awaiting review
**Автор:** brainstorming session
**Связанные артефакты:** бэкенд `src/main/java/com/github/interviewbeaterservice/{auth,user,config}/`

## Цель

SPA-фронтенд, который реализует регистрацию и логин в стиле Atlassian (id.atlassian.com/login) и полноценную работу с JWT: хранение access + refresh, автоматический refresh, retry 401-запросов, защита маршрутов, logout.

## Объём

**В скоупе:**
- Форма регистрации (email + password) на `/signup`.
- Форма логина (email + password) на `/login`.
- Защищённый список вопросов на `/` (использует существующий бэкенд API `/questions`).
- JWT-флоу: in-memory access, refresh в localStorage, авто-refresh по 401 и по таймеру.
- Logout.
- Минимальные правки бэкенда (3 пункта, см. ниже).

**Вне скоупа:**
- Социальные логины, 2FA, подтверждение email, сброс пароля.
- Внутренние страницы приложения (только список вопросов как демонстрация).
- E2E-тесты.
- Production-сборка / Docker-образ фронта (dev-режим достаточен для проверки задачи).

## Технологический стек

| Слой             | Выбор                                              |
|------------------|----------------------------------------------------|
| Сборка / dev     | Vite 5 + TypeScript (strict)                       |
| UI-фреймворк     | React 18                                           |
| UI-компоненты    | shadcn/ui (копируемые в проект, без рантайм-зависимости) |
| Стилизация       | Tailwind CSS                                       |
| Роутинг          | react-router-dom v6                                |
| Формы            | react-hook-form + zod                              |
| HTTP             | axios с кастомным interceptor-ом                   |
| State            | Zustand (один auth-стор)                           |
| Тесты            | Vitest + React Testing Library                     |

## Структура проекта

```
frontend/
├── package.json
├── vite.config.ts                  # dev-proxy /auth, /questions → :8080
├── tsconfig.json
├── tailwind.config.ts
├── postcss.config.js
├── index.html
├── components.json                 # shadcn/ui config
└── src/
    ├── main.tsx                    # mount + AuthBootstrapper
    ├── App.tsx                     # Router + routes
    ├── index.css                   # tailwind directives
    ├── api/
    │   ├── client.ts               # axios instance + interceptor
    │   ├── auth.ts                 # login, signup, refresh
    │   └── questions.ts            # listQuestions()
    ├── auth/
    │   ├── store.ts                # Zustand: accessToken, user, refresh
    │   ├── useAuth.ts              # хук доступа к стору
    │   ├── refreshScheduler.ts     # таймер refresh по exp
    │   ├── AuthBootstrapper.tsx    # попытка восстановить сессию при старте
    │   └── ProtectedRoute.tsx      # guard
    ├── components/
    │   ├── ui/                     # shadcn: Button, Input, Label, Card
    │   ├── AuthLayout.tsx          # центрированная карточка на фоне
    │   ├── AuthForm.tsx            # переиспользуемая форма (login/signup)
    │   └── LogoutButton.tsx
    ├── pages/
    │   ├── LoginPage.tsx
    │   ├── SignupPage.tsx
    │   └── HomePage.tsx            # защищённая, список вопросов
    └── lib/
        ├── utils.ts                # cn() helper
        └── jwt.ts                  # parseExp() — достать exp из JWT без verify
```

## Контракт API (после правок бэкенда)

Существующий бэкенд после трёх правок:

| Метод | URL              | Тело запроса                                   | Тело ответа                                         | Статусы          |
|-------|------------------|------------------------------------------------|-----------------------------------------------------|------------------|
| POST  | `/auth/signup`   | `{email: string, password: string}`            | `RegisterResponse { id: number }`                   | 200, 400, 409    |
| POST  | `/auth/login`    | `{email: string, password: string}`            | `LoginResponse { accessToken, refreshToken, userId, email, role }` | 200, 400, 401 |
| POST  | `/auth/refresh`  | `{refreshToken: string}`                        | `LoginResponse`                                     | 200, 401         |
| GET   | `/questions`     | —                                              | (существующий ответ бэка — список вопросов)        | 200, 401         |

**Ошибки:** `{error: string, fields?: Array<{field: string, message: string}>}`. 401 на `/questions` — фронт триггерит refresh.

## Флоу пользователя

### Регистрация
1. `/signup`. Поля: email, password (одно поле, без confirm — упрощение).
2. Submit → POST `/auth/signup`.
3. 200: редирект на `/login?email=<тот же email>` (auto-fill через query string).
4. 400 (validation): показать ошибки полей из `fields[]`.
5. 409 (email exists): ошибка "Email already registered" под полем email.

### Логин
1. `/login`. Поля: email, password. Чекбокс "Stay signed in" — НЕ в скоупе (только UI-заглушка без логики).
2. Submit → POST `/auth/login`.
3. 200: сохранить `accessToken` в store, `refreshToken` в localStorage, user info в store. Запланировать refresh по таймеру. Redirect → `/`.
4. 401: "Incorrect email or password" под формой.

### Home (защищённая)
1. Если `accessToken` в store — fetch `/questions`. Отрендерить список (id + текст первых 80 символов).
2. Если 401 — interceptor сделает refresh и повторит. Если refresh неуспешен — logout + redirect `/login?reason=expired`.

### Logout
1. Очистить store, localStorage `auth_refresh`, отменить `setTimeout` refreshScheduler.
2. Redirect `/login`.

## JWT-флоу (детально)

### Хранение
- `accessToken`: in-memory только (Zustand). При F5 — пропадает, регенерируется через `/auth/refresh` из localStorage.
- `refreshToken`: `localStorage["auth_refresh"]`. Удаляется при logout и при неуспехе refresh.
- User info (id, email, role): в Zustand, не персистится.

### Interceptor (axios)
```
onRequest:
  attach Authorization: Bearer <accessToken> if present

onResponseError:
  if status !== 401 → reject
  if !originalRequest._retry → mark _retry = true
  enqueue originalRequest in waiters
  if refreshInFlight → return waiter promise
  refreshInFlight = POST /auth/refresh with current refreshToken
  on success:
    store new accessToken, refreshToken
    update refreshScheduler
    resolve all waiters with originalRequest retried
    refreshInFlight = null
  on failure:
    logout (clear store, localStorage, redirect /login)
    reject all waiters
```

Защита от N параллельных refresh-ов: один in-flight promise, очередь ждущих.

### refreshScheduler
- Парсит `exp` из access JWT (`atob` payload без verify — нам нужна только дата).
- Ставит `setTimeout(за 60 сек до exp, refresh())`. 
- При logout — `clearTimeout`.
- При успешном ручном refresh — отменяет старый таймер, ставит новый.

## UI: стиль Atlassian

- Белый фон (`bg-white` или `bg-gray-50`), без иллюстраций.
- Логотип приложения (текстовый placeholder "InterviewBeater") сверху.
- Карточка шириной ~400px по центру, padding, скруглённые углы, тонкая тень.
- Поля: border, focus-ring синего цвета (`ring-blue-500`).
- Кнопка Continue — полная ширина, синяя (`bg-blue-600`), белый текст, disabled при `isSubmitting`.
- Ссылка внизу карточки: "Already have an account? Log in" / "Don't have an account? Sign up".
- Текст ошибки — красный, под полем или над кнопкой.

## Правки бэкенда (минимальные)

1. **`UserController.signup`** — возвращать `RegisterResponse` (record `{Long id}`) вместо `String.toString()`. Это меняет тело ответа с `text/plain;123` на `application/json;{"id":123}`.
2. **`SecurityConfig`** — добавить бин `CorsConfigurationSource`:
   - `allowedOrigins`: `http://localhost:5173`
   - `allowedMethods`: `GET, POST, PUT, DELETE, OPTIONS`
   - `allowedHeaders`: `*`
   - `allowCredentials`: `false`
   - В `securityFilterChain` добавить `.cors(Customizer.withDefaults())`.
3. **`ControllerAdvice`** — добавить:
   - `BadCredentialsException` → 401 `{error: "Invalid email or password"}`.
   - `DataIntegrityViolationException` → 409 `{error: "Email already registered"}`.

Эти три изменения не затрагивают модель данных, безопасность или схему БД.

## Тестирование

| Уровень     | Что покрываем                                                                                  |
|-------------|------------------------------------------------------------------------------------------------|
| Unit        | `refreshScheduler`: парсинг exp, отмена, перепланирование.                                      |
| Unit        | `client.ts` interceptor: 401 → refresh → retry; refresh fail → logout + reject всех waiter-ов. |
| Integration | `AuthForm` (login): submit → success → redirect; 401 → ошибка под формой; 400 → ошибки полей.  |
| Integration | `ProtectedRoute`: пустой стор → redirect `/login`; непустой → рендер children.                 |

Тесты мокают axios (MSW или ручной jest.mock).

## Безопасность и компромиссы

- **localStorage для refresh** — допустимо для MVP, документируется в `frontend/README.md` как known trade-off. Production-усиление (httpOnly cookie + CSRF) — отдельная задача.
- **Нет защиты от XSS через dangerouslySetInnerHTML** — не используется в формах.
- **Пароль в открытом виде по HTTPS в проде** — ответственность деплоя, не фронта.
- **Таймер refresh по клиентскому exp** — если системные часы пользователя сильно сбиты, refresh может сработать раньше/позже. На этот случай есть fallback по 401.

## Открытые вопросы

Нет.

## Критерии приёмки

- [ ] `/signup`: ввод валидного email+password → редирект на `/login`.
- [ ] `/signup`: дубликат email → ошибка "Email already registered" под полем.
- [ ] `/login`: валидные креды → редирект на `/`, fetch `/questions` отображает список.
- [ ] `/login`: неверный пароль → "Invalid email or password".
- [ ] F5 на `/` (с активной сессией) → сессия восстанавливается через `/auth/refresh`.
- [ ] Ручное истечение access (или установка короткого TTL в dev) → фоновый refresh за 60 сек до exp, без видимой задержки для пользователя.
- [ ] Logout очищает localStorage и стор, refresh после logout невозможен.
- [ ] Vitest прогоняется, ключевые тесты зелёные.
- [ ] `npm run build` собирает прод-бандл без ошибок типов.
