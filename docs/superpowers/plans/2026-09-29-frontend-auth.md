# Frontend Auth (Registration Form) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Собрать SPA-фронтенд (React + Vite + TS) с формой регистрации/логина в стиле Atlassian и полным JWT-флоу (access in-memory, refresh в localStorage, авто-refresh по 401 + по таймеру), который дёргает существующий бэкенд `interviewBeaterService`.

**Architecture:** Один SPA на React Router. AuthBootstrapper восстанавливает сессию при старте через `/auth/refresh`. axios interceptor ловит 401, синхронно делает refresh (с очередью waiter-ов, чтобы не было N параллельных refresh-ов) и повторяет исходный запрос. refreshScheduler планирует фоновый refresh за 60 сек до `exp` access-токена. Хранение: access в Zustand (in-memory), refresh в localStorage. Минимальные правки бэкенда: фикс контракта `/auth/signup`, CORS для `:5173`, два новых exception handler-а.

**Tech Stack:** Vite 5, React 18, TypeScript strict, react-router-dom v6, Tailwind CSS, shadcn/ui (копируемые компоненты), react-hook-form + zod, axios, Zustand, Vitest + RTL.

**Spec:** `docs/superpowers/specs/2026-09-29-frontend-auth-design.md`

## Global Constraints

- TypeScript strict mode (`"strict": true` в `tsconfig.json`).
- Node 20+ для dev (`package.json` engines `"node": ">=20"`).
- Цветовая палитра UI: только Tailwind defaults + `blue-600` для primary (никаких кастомных hex без нужды).
- Все запросы к API идут через единый axios instance из `src/api/client.ts` — никакого прямого `fetch`.
- Access-токен **никогда** не пишется в localStorage/sessionStorage — только в Zustand (in-memory).
- Refresh-токен читается из localStorage по ключу ровно `auth_refresh`.
- Все коммиты на русском commit-message как в существующей истории проекта; prefix: `feat (Frontend):`, `feat (Auth):`, `fix (Auth):`, `chore (Frontend):`, `test (Frontend):`.
- `Co-Authored-By: Claude Code <noreply@anthropic.com>` в каждом коммите.

## Review Focus

Спек описывает happy path и две ошибки (401, 409). Этот план покрывает их тестами. Но эти входные классы легко упустить, и именно они сожгут пользователя:

1. **Параллельные 401 на разных запросах** — без единого in-flight refresh пользователь улетит в logout при первом же «холостом» 401. Закрыто тестом «concurrent 401s share one refresh».
2. **Refresh после logout** — если пользователь нажал Logout, пока в полёте висит refresh, приложение не должно залогинить его обратно. Закрыто тестом «logout during in-flight refresh».
3. **F5 на защищённой странице без refresh в localStorage** — AuthBootstrapper не должен зацикливаться (redirect → re-render → redirect). Закрыто тестом «AuthBootstrapper falls through when no refresh».
4. **Системные часы пользователя сбиты** — если `exp` из JWT в прошлом, refreshScheduler не должен уйти в бесконечный retry; должен сразу триггернуть refresh или logout. Закрыто тестом «refreshScheduler handles already-expired token».
5. **Регистрация с дубликатом email при неуникальном constraint** — бэк сейчас отдаёт 500 на `DataIntegrityViolationException`; нужно проверить, что ControllerAdvice ловит именно его, а не родительский `Exception`. Закрыто тестом бэка «duplicate email returns 409».

---

## File Structure

Создаёмые файлы — все под `frontend/`, кроме трёх правок бэка:

**Бэкенд (правки):**
- `src/main/java/com/github/interviewbeaterservice/user/controller/UserController.java` — изменить return type.
- `src/main/java/com/github/interviewbeaterservice/config/SecurityConfig.java` — добавить CORS.
- `src/main/java/com/github/interviewbeaterservice/config/ControllerAdvice.java` — добавить два handler-а.

**Фронтенд (новое):**
```
frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json
├── tsconfig.node.json
├── tailwind.config.ts
├── postcss.config.js
├── index.html
├── .gitignore
├── README.md
├── components.json
├── src/
│   ├── main.tsx
│   ├── App.tsx
│   ├── index.css
│   ├── vite-env.d.ts
│   ├── api/
│   │   ├── client.ts
│   │   ├── auth.ts
│   │   └── questions.ts
│   ├── auth/
│   │   ├── store.ts
│   │   ├── useAuth.ts
│   │   ├── refreshScheduler.ts
│   │   ├── AuthBootstrapper.tsx
│   │   └── ProtectedRoute.tsx
│   ├── components/
│   │   ├── ui/button.tsx
│   │   ├── ui/input.tsx
│   │   ├── ui/label.tsx
│   │   ├── ui/card.tsx
│   │   ├── AuthLayout.tsx
│   │   ├── AuthForm.tsx
│   │   └── LogoutButton.tsx
│   ├── pages/
│   │   ├── LoginPage.tsx
│   │   ├── SignupPage.tsx
│   │   └── HomePage.tsx
│   └── lib/
│       ├── utils.ts
│       └── jwt.ts
└── src/__tests__/
    ├── refreshScheduler.test.ts
    ├── client.interceptor.test.ts
    ├── AuthForm.test.tsx
    └── ProtectedRoute.test.tsx
```

**Backend тесты:**
- `src/test/java/com/github/interviewbeaterservice/config/ControllerAdviceTest.java` — проверяет, что `BadCredentialsException` → 401, `DataIntegrityViolationException` → 409.

---

## Tasks

### Task 1: Backend — fix `/auth/signup` return type

**Files:**
- Modify: `src/main/java/com/github/interviewbeaterservice/user/controller/UserController.java:26-30`
- Modify: `src/test/java/com/github/interviewbeaterservice/auth/AuthControllerTest.java` (если есть тест signup-а, проверить, что он не сломается; новых тестов не пишем — контракт всё ещё «вернуть id», просто как JSON)

**Context:** Сейчас метод `signup` возвращает `String` (id пользователя, сериализованный через `toString()`). Фронт ожидает JSON `{"id": <number>}`.

**Interfaces:**
- Consumes: `RegisterRequest{email, password}` (без изменений).
- Produces: HTTP 200 с телом `{"id": 1}` (тип `RegisterResponse`).

- [ ] **Step 1: Read the current controller**

Read `src/main/java/com/github/interviewbeaterservice/user/controller/UserController.java`. Подтвердить, что метод `signup` возвращает `String`.

- [ ] **Step 2: Change return type to RegisterResponse**

В `src/main/java/com/github/interviewbeaterservice/user/controller/UserController.java` заменить метод `signup`:

```java
@Operation(summary = "Регистрация нового пользователя, возвращает id")
@PostMapping("/signup")
public RegisterResponse signup(@RequestBody @Valid RegisterRequest requestBody) {
    User createdUser = userService.register(requestBody.email(), requestBody.password());

    return new RegisterResponse(createdUser.getId());
}
```

- [ ] **Step 3: Verify import**

Убедиться, что в импортах файла есть `import com.github.interviewbeaterservice.user.dto.RegisterResponse;`. Если нет — добавить.

- [ ] **Step 4: Build backend**

Run: `./mvnw -q -DskipTests compile`
Expected: BUILD SUCCESS, без ошибок.

- [ ] **Step 5: Run existing auth tests**

Run: `./mvnw -q -Dtest=AuthControllerTest test`
Expected: PASS (existing tests проверяют логин/refresh, не signup; если есть signup-тест — он может упасть, и тогда правим тест).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/github/interviewbeaterservice/user/controller/UserController.java
git commit -m "feat (Auth): signup возвращает RegisterResponse вместо String

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 2: Backend — add CORS for :5173

**Files:**
- Modify: `src/main/java/com/github/interviewbeaterservice/config/SecurityConfig.java`

**Context:** Фронт на Vite dev-сервере `:5173` ходит к бэку `:8080`. Без CORS браузер блокирует кросс-доменные запросы. Credentials не нужны (токены в localStorage, не в cookie).

- [ ] **Step 1: Read SecurityConfig**

Read `src/main/java/com/github/interviewbeaterservice/config/SecurityConfig.java`.

- [ ] **Step 2: Add CorsConfigurationSource bean**

Добавить в класс `SecurityConfig` (после бина `passwordEncoder`, до `securityFilterChain`):

```java
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOrigins(List.of("http://localhost:5173"));
    cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setAllowCredentials(false);
    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
}
```

- [ ] **Step 3: Wire CORS into filter chain**

В бине `securityFilterChain` после `.csrf(csrf -> csrf.disable())` добавить:

```java
.cors(cors -> cors.configurationSource(corsConfigurationSource()))
```

И добавить импорт `import org.springframework.security.config.Customizer;` (если ещё нет).

- [ ] **Step 4: Build backend**

Run: `./mvnw -q -DskipTests compile`
Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/github/interviewbeaterservice/config/SecurityConfig.java
git commit -m "feat (Auth): CORS для Vite dev-сервера :5173

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 3: Backend — handle BadCredentials + DataIntegrityViolation in ControllerAdvice

**Files:**
- Modify: `src/main/java/com/github/interviewbeaterservice/config/ControllerAdvice.java`
- Create: `src/test/java/com/github/interviewbeaterservice/config/ControllerAdviceTest.java`

**Context:** Сейчас `BadCredentialsException` (кидается на неверный логин/refresh) не имеет handler-а → 500. `DataIntegrityViolationException` (дубликат email при signup) тоже → 500. Нужны 401 и 409 соответственно.

- [ ] **Step 1: Read ControllerAdvice**

Read `src/main/java/com/github/interviewbeaterservice/config/ControllerAdvice.java`.

- [ ] **Step 2: Read BadCredentialsException**

Read `src/main/java/com/github/interviewbeaterservice/auth/exception/BadCredentialsException.java` чтобы знать точный путь импорта.

- [ ] **Step 3: Add imports and handlers**

В начало файла `ControllerAdvice.java` добавить импорты:

```java
import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import org.springframework.dao.DataIntegrityViolationException;
```

В тело класса добавить два handler-а (порядок — рядом с другими handler-ами):

```java
@ExceptionHandler(BadCredentialsException.class)
public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
    return body(HttpStatus.UNAUTHORIZED, "Invalid email or password");
}

@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<Map<String, String>> handleDataIntegrity(DataIntegrityViolationException e) {
    return body(HttpStatus.CONFLICT, "Email already registered");
}
```

- [ ] **Step 4: Write failing test**

Создать `src/test/java/com/github/interviewbeaterservice/config/ControllerAdviceTest.java`:

```java
package com.github.interviewbeaterservice.config;

import com.github.interviewbeaterservice.auth.exception.BadCredentialsException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ControllerAdviceTest {

    private final ControllerAdvice advice = new ControllerAdvice();

    @Test
    void badCredentials_returns401() {
        ResponseEntity<Map<String, String>> response = advice.handleBadCredentials(new BadCredentialsException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "Invalid email or password");
    }

    @Test
    void dataIntegrityViolation_returns409() {
        ResponseEntity<Map<String, String>> response = advice.handleDataIntegrity(new DataIntegrityViolationException("dup"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("error", "Email already registered");
    }
}
```

- [ ] **Step 5: Run new test to verify it passes**

Run: `./mvnw -q -Dtest=ControllerAdviceTest test`
Expected: PASS (оба теста зелёные).

- [ ] **Step 6: Run all tests to verify nothing else broke**

Run: `./mvnw -q test`
Expected: PASS (никаких regressions).

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/github/interviewbeaterservice/config/ControllerAdvice.java src/test/java/com/github/interviewbeaterservice/config/ControllerAdviceTest.java
git commit -m "feat (Auth): 401 на BadCredentials, 409 на дубликат email

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 4: Frontend — scaffold Vite + React + TS project

**Files:**
- Create: `frontend/` directory with all scaffolding files (Vite, TS, Tailwind, shadcn config, base structure)

**Context:** Создаём корневую структуру `frontend/`. Зависимости ставятся здесь; никаких изменений в корне репо кроме добавления `frontend/`.

- [ ] **Step 1: Create frontend directory and package.json**

Run:
```bash
mkdir -p frontend/src
```

Write `frontend/package.json`:

```json
{
  "name": "interview-beater-frontend",
  "private": true,
  "version": "0.1.0",
  "type": "module",
  "engines": { "node": ">=20" },
  "scripts": {
    "dev": "vite",
    "build": "tsc -b && vite build",
    "preview": "vite preview",
    "lint": "tsc --noEmit",
    "test": "vitest run",
    "test:watch": "vitest"
  },
  "dependencies": {
    "@hookform/resolvers": "^3.9.0",
    "axios": "^1.7.7",
    "clsx": "^2.1.1",
    "react": "^18.3.1",
    "react-dom": "^18.3.1",
    "react-hook-form": "^7.53.0",
    "react-router-dom": "^6.27.0",
    "tailwind-merge": "^2.5.4",
    "zod": "^3.23.8",
    "zustand": "^4.5.5"
  },
  "devDependencies": {
    "@testing-library/jest-dom": "^6.5.0",
    "@testing-library/react": "^16.0.1",
    "@testing-library/user-event": "^14.5.2",
    "@types/react": "^18.3.11",
    "@types/react-dom": "^18.3.0",
    "@vitejs/plugin-react": "^4.3.2",
    "autoprefixer": "^10.4.20",
    "jsdom": "^25.0.1",
    "postcss": "^8.4.47",
    "tailwindcss": "^3.4.13",
    "typescript": "^5.6.2",
    "vite": "^5.4.8",
    "vitest": "^2.1.2"
  }
}
```

- [ ] **Step 2: Write tsconfig.json**

Write `frontend/tsconfig.json`:

```json
{
  "compilerOptions": {
    "target": "ES2022",
    "useDefineForClassFields": true,
    "lib": ["ES2022", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": false,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "moduleDetection": "force",
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "types": ["vitest/globals", "@testing-library/jest-dom"],
    "esModuleInterop": true
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 3: Write tsconfig.node.json**

Write `frontend/tsconfig.node.json`:

```json
{
  "compilerOptions": {
    "composite": true,
    "skipLibCheck": true,
    "module": "ESNext",
    "moduleResolution": "bundler",
    "allowSyntheticDefaultImports": true,
    "strict": true
  },
  "include": ["vite.config.ts"]
}
```

- [ ] **Step 4: Write vite.config.ts**

Write `frontend/vite.config.ts`:

```ts
/// <reference types="vitest" />
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: { '@': path.resolve(__dirname, 'src') },
  },
  server: {
    port: 5173,
    proxy: {
      '/auth':    { target: 'http://localhost:8080', changeOrigin: false },
      '/questions': { target: 'http://localhost:8080', changeOrigin: false },
    },
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: ['./src/test-setup.ts'],
  },
});
```

- [ ] **Step 5: Write tailwind.config.ts, postcss.config.js, components.json**

Write `frontend/tailwind.config.ts`:

```ts
import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: { extend: {} },
  plugins: [],
} satisfies Config;
```

Write `frontend/postcss.config.js`:

```js
export default {
  plugins: { tailwindcss: {}, autoprefixer: {} },
};
```

Write `frontend/components.json`:

```json
{
  "$schema": "https://ui.shadcn.com/schema.json",
  "style": "default",
  "rsc": false,
  "tsx": true,
  "tailwind": {
    "config": "tailwind.config.ts",
    "css": "src/index.css",
    "baseColor": "slate",
    "cssVariables": false
  },
  "aliases": {
    "components": "@/components",
    "utils": "@/lib/utils"
  }
}
```

- [ ] **Step 6: Write index.html, .gitignore, README**

Write `frontend/index.html`:

```html
<!doctype html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>InterviewBeater</title>
  </head>
  <body class="bg-gray-50">
    <div id="root"></div>
    <script type="module" src="/src/main.tsx"></script>
  </body>
</html>
```

Write `frontend/.gitignore`:

```
node_modules
dist
.vite
*.log
```

Write `frontend/README.md`:

```markdown
# InterviewBeater Frontend

SPA на React + Vite + TS.

## Запуск

```bash
cd frontend
npm install
npm run dev    # http://localhost:5173, проксирует /auth, /questions на :8080
```

Бэкенд должен быть запущен на `http://localhost:8080`.

## Стек

- React 18 + TypeScript strict
- Vite 5
- react-router-dom v6
- Tailwind CSS + shadcn/ui (копируемые компоненты)
- react-hook-form + zod
- axios + Zustand
- Vitest + RTL

## Безопасность

Access-токен хранится **только в памяти** (Zustand). Refresh-токен — в `localStorage["auth_refresh"]`. Это компромисс для MVP: production должен использовать httpOnly cookie + CSRF.

## Тесты

```bash
npm test
```
```

- [ ] **Step 7: Install dependencies**

Run:
```bash
cd frontend
npm install
```

Expected: без ошибок, `node_modules/` и `package-lock.json` созданы.

- [ ] **Step 8: Commit**

```bash
git add frontend/package.json frontend/package-lock.json frontend/tsconfig.json frontend/tsconfig.node.json frontend/vite.config.ts frontend/tailwind.config.ts frontend/postcss.config.js frontend/components.json frontend/index.html frontend/.gitignore frontend/README.md
git commit -m "chore (Frontend): scaffold Vite + React + TS + Tailwind + shadcn config

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 5: Frontend — lib utilities (cn + jwt parser)

**Files:**
- Create: `frontend/src/lib/utils.ts`
- Create: `frontend/src/lib/jwt.ts`
- Create: `frontend/src/test-setup.ts`
- Create: `frontend/src/vite-env.d.ts`
- Create: `frontend/src/index.css`
- Create: `frontend/src/__tests__/jwt.test.ts`

**Context:** Базовые утилиты. `cn` — стандартный helper для классов из shadcn. `jwt.parseExp` — достаёт `exp` из JWT без верификации (только для планирования таймера).

**Interfaces:**
- `cn(...inputs: ClassValue[]): string` — мержит Tailwind-классы.
- `parseExp(token: string): number | null` — возвращает секунды Unix или null, если токен битый.

- [ ] **Step 1: Write test-setup and vite-env**

Write `frontend/src/vite-env.d.ts`:

```ts
/// <reference types="vite/client" />
```

Write `frontend/src/test-setup.ts`:

```ts
import '@testing-library/jest-dom/vitest';
```

Write `frontend/src/index.css`:

```css
@tailwind base;
@tailwind components;
@tailwind utilities;
```

- [ ] **Step 2: Write utils.ts**

Write `frontend/src/lib/utils.ts`:

```ts
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]): string {
  return twMerge(clsx(inputs));
}
```

- [ ] **Step 3: Write failing test for parseExp**

Write `frontend/src/__tests__/jwt.test.ts`:

```ts
import { describe, it, expect } from 'vitest';
import { parseExp } from '@/lib/jwt';

const base64url = (obj: object) =>
  btoa(JSON.stringify(obj)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');

const makeJwt = (payload: object) =>
  `header.${base64url(payload)}.signature`;

describe('parseExp', () => {
  it('returns exp seconds when valid', () => {
    const token = makeJwt({ exp: 1_700_000_000 });
    expect(parseExp(token)).toBe(1_700_000_000);
  });

  it('returns null when exp missing', () => {
    const token = makeJwt({ sub: '1' });
    expect(parseExp(token)).toBeNull();
  });

  it('returns null for malformed token', () => {
    expect(parseExp('not.a.jwt')).toBeNull();
    expect(parseExp('garbage')).toBeNull();
  });
});
```

- [ ] **Step 4: Run test to verify it fails**

Run:
```bash
cd frontend
npx vitest run src/__tests__/jwt.test.ts
```
Expected: FAIL — module not found.

- [ ] **Step 5: Implement parseExp**

Write `frontend/src/lib/jwt.ts`:

```ts
export function parseExp(token: string): number | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = parts[1];
    const padded = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = atob(padded);
    const obj = JSON.parse(json) as { exp?: unknown };
    return typeof obj.exp === 'number' ? obj.exp : null;
  } catch {
    return null;
  }
}
```

- [ ] **Step 6: Run test to verify it passes**

Run: `npx vitest run src/__tests__/jwt.test.ts`
Expected: PASS, 3 теста зелёные.

- [ ] **Step 7: Commit**

```bash
git add frontend/src/lib frontend/src/test-setup.ts frontend/src/vite-env.d.ts frontend/src/index.css frontend/src/__tests__/jwt.test.ts
git commit -m "feat (Frontend): cn() и parseExp() утилиты

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 6: Frontend — auth store (Zustand)

**Files:**
- Create: `frontend/src/auth/store.ts`
- Create: `frontend/src/auth/useAuth.ts`

**Context:** Zustand-стор для auth. Хранит `accessToken` (in-memory), `user` (id/email/role), и helper-методы. Refresh хранится **только** в localStorage, не в сторе.

**Interfaces:**
- `useAuthStore` — Zustand store с полями:
  - `accessToken: string | null`
  - `user: { id: number; email: string; role: string } | null`
  - `setSession({ accessToken, user }): void`
  - `setAccessToken(token: string): void`
  - `clear(): void`
- `useAuth()` — селектор-хук, возвращает `{ isAuthenticated: boolean; user, accessToken, setSession, setAccessToken, clear }`.

- [ ] **Step 1: Write store.ts**

Write `frontend/src/auth/store.ts`:

```ts
import { create } from 'zustand';

export interface AuthUser {
  id: number;
  email: string;
  role: string;
}

interface AuthState {
  accessToken: string | null;
  user: AuthUser | null;
  setSession: (input: { accessToken: string; user: AuthUser }) => void;
  setAccessToken: (token: string) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: null,
  user: null,
  setSession: ({ accessToken, user }) => set({ accessToken, user }),
  setAccessToken: (accessToken) => set({ accessToken }),
  clear: () => set({ accessToken: null, user: null }),
}));

export const REFRESH_KEY = 'auth_refresh';

export function readRefresh(): string | null {
  return localStorage.getItem(REFRESH_KEY);
}

export function writeRefresh(token: string): void {
  localStorage.setItem(REFRESH_KEY, token);
}

export function clearRefresh(): void {
  localStorage.removeItem(REFRESH_KEY);
}
```

- [ ] **Step 2: Write useAuth.ts hook**

Write `frontend/src/auth/useAuth.ts`:

```ts
import { useAuthStore } from './store';

export function useAuth() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const user = useAuthStore((s) => s.user);
  const setSession = useAuthStore((s) => s.setSession);
  const setAccessToken = useAuthStore((s) => s.setAccessToken);
  const clear = useAuthStore((s) => s.clear);

  return {
    isAuthenticated: accessToken !== null,
    accessToken,
    user,
    setSession,
    setAccessToken,
    clear,
  };
}
```

- [ ] **Step 3: Verify build**

Run: `cd frontend && npx tsc --noEmit`
Expected: без ошибок типов.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/auth/store.ts frontend/src/auth/useAuth.ts
git commit -m "feat (Frontend): Zustand auth-стор с accessToken in-memory

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 7: Frontend — refreshScheduler

**Files:**
- Create: `frontend/src/auth/refreshScheduler.ts`
- Create: `frontend/src/__tests__/refreshScheduler.test.ts`

**Context:** Планирует `setTimeout(refresh, exp - now - 60_000)`. Отменяемый. Если `exp` уже в прошлом — немедленно вызывает колбэк.

**Interfaces:**
- `scheduleRefresh(expSeconds: number, onExpire: () => void): () => void` — возвращает cancel-функцию.

- [ ] **Step 1: Write failing tests**

Write `frontend/src/__tests__/refreshScheduler.test.ts`:

```ts
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { scheduleRefresh } from '@/auth/refreshScheduler';

beforeEach(() => vi.useFakeTimers());
afterEach(() => vi.useRealTimers());

describe('scheduleRefresh', () => {
  it('fires onExpire 60s before exp', () => {
    const cb = vi.fn();
    const now = 1_000_000;
    vi.setSystemTime(now);

    scheduleRefresh(now + 600, cb); // exp через 600 сек, refresh за 60 сек до = через 540 сек

    vi.advanceTimersByTime(539_999);
    expect(cb).not.toHaveBeenCalled();

    vi.advanceTimersByTime(1);
    expect(cb).toHaveBeenCalledOnce();
  });

  it('fires immediately when exp is in the past', () => {
    const cb = vi.fn();
    vi.setSystemTime(1_000_000);

    scheduleRefresh(999_000, cb);

    expect(cb).toHaveBeenCalledOnce();
  });

  it('cancel function prevents the callback', () => {
    const cb = vi.fn();
    vi.setSystemTime(1_000_000);

    const cancel = scheduleRefresh(1_000_000 + 600, cb);
    cancel();

    vi.advanceTimersByTime(1_000_000);
    expect(cb).not.toHaveBeenCalled();
  });

  it('cancel is safe to call after fire', () => {
    const cb = vi.fn();
    vi.setSystemTime(1_000_000);
    const cancel = scheduleRefresh(999_000, cb); // fires immediately
    expect(() => cancel()).not.toThrow();
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd frontend && npx vitest run src/__tests__/refreshScheduler.test.ts`
Expected: FAIL — module not found.

- [ ] **Step 3: Implement refreshScheduler**

Write `frontend/src/auth/refreshScheduler.ts`:

```ts
const REFRESH_LEAD_MS = 60_000; // refresh за 60 сек до exp

export function scheduleRefresh(
  expSeconds: number,
  onExpire: () => void,
): () => void {
  const nowMs = Date.now();
  const expMs = expSeconds * 1000;
  const delay = expMs - nowMs - REFRESH_LEAD_MS;

  if (delay <= 0) {
    // exp уже близко или в прошлом — стреляем немедленно (на следующем тике, чтобы не stack-overflow)
    const handle = setTimeout(onExpire, 0);
    return () => clearTimeout(handle);
  }

  const handle = setTimeout(onExpire, delay);
  return () => clearTimeout(handle);
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `cd frontend && npx vitest run src/__tests__/refreshScheduler.test.ts`
Expected: PASS, 4 теста зелёные.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/auth/refreshScheduler.ts frontend/src/__tests__/refreshScheduler.test.ts
git commit -m "feat (Frontend): refreshScheduler с отменой и past-exp фолбэком

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 8: Frontend — axios client with 401 interceptor

**Files:**
- Create: `frontend/src/api/client.ts`
- Create: `frontend/src/__tests__/client.interceptor.test.ts`

**Context:** Единая точка HTTP. Interceptor ловит 401 → дёргает `/auth/refresh` → повторяет исходный запрос. Один in-flight refresh, очередь waiter-ов. На неуспех refresh — logout.

**Interfaces:**
- `api: AxiosInstance` — singleton с interceptor-ами.
- `attemptRefresh(): Promise<boolean>` — экспортируется для AuthBootstrapper и refreshScheduler.
- `logoutAndRedirect(): void` — экспортируется для LogoutButton.

- [ ] **Step 1: Write failing tests**

Write `frontend/src/__tests__/client.interceptor.test.ts`:

```ts
import { describe, it, expect, vi, beforeEach } from 'vitest';

// Mock store + scheduler + api/auth до импорта client
vi.mock('@/auth/store', () => ({
  useAuthStore: {
    getState: vi.fn(),
  },
  REFRESH_KEY: 'auth_refresh',
  readRefresh: vi.fn(),
  writeRefresh: vi.fn(),
  clearRefresh: vi.fn(),
}));

vi.mock('@/auth/refreshScheduler', () => ({
  scheduleRefresh: vi.fn(() => () => undefined),
}));

vi.mock('@/api/auth', () => ({
  refreshSession: vi.fn(),
  LoginResponse: class {},
}));

import { api } from '@/api/client';
import { useAuthStore, readRefresh, writeRefresh, clearRefresh } from '@/auth/store';
import { refreshSession } from '@/api/auth';

const mockedUseAuthStore = vi.mocked(useAuthStore);
const mockedReadRefresh = vi.mocked(readRefresh);
const mockedWriteRefresh = vi.mocked(writeRefresh);
const mockedClearRefresh = vi.mocked(clearRefresh);
const mockedRefreshSession = vi.mocked(refreshSession);

beforeEach(() => {
  vi.clearAllMocks();
  // дефолт: пустой access, нет refresh
  mockedUseAuthStore.getState.mockReturnValue({ accessToken: null, user: null, setSession: vi.fn(), setAccessToken: vi.fn(), clear: vi.fn() });
  mockedReadRefresh.mockReturnValue(null);
});

describe('axios 401 interceptor', () => {
  it('attaches Authorization header when accessToken present', async () => {
    mockedUseAuthStore.getState.mockReturnValue({
      accessToken: 'ACCESS',
      user: null,
      setSession: vi.fn(),
      setAccessToken: vi.fn(),
      clear: vi.fn(),
    });

    const adapter = vi.fn().mockResolvedValue({ status: 200, data: 'ok', headers: {}, config: { url: '/x' } });
    (api.defaults.adapter as unknown) = adapter;

    await api.get('/x');
    expect(adapter.mock.calls[0][0].headers.Authorization).toBe('Bearer ACCESS');
  });

  it('on 401 refreshes and retries original request', async () => {
    mockedReadRefresh.mockReturnValue('REFRESH');
    mockedRefreshSession.mockResolvedValue({
      accessToken: 'NEW_ACCESS',
      refreshToken: 'NEW_REFRESH',
      userId: 1, email: 'a@b.c', role: 'USER',
    });
    const setAccessToken = vi.fn();
    mockedUseAuthStore.getState.mockReturnValue({
      accessToken: 'OLD_ACCESS',
      user: null,
      setSession: vi.fn(),
      setAccessToken,
      clear: vi.fn(),
    });

    let call = 0;
    const adapter = vi.fn().mockImplementation((config) => {
      call += 1;
      if (config.url === '/x' && call === 1) {
        const err = new Error('401') as Error & { response: { status: number; data: unknown; headers: {}; config: typeof config } };
        err.response = { status: 401, data: null, headers: {}, config };
        return Promise.reject(err);
      }
      return Promise.resolve({ status: 200, data: 'ok', headers: {}, config });
    });
    (api.defaults.adapter as unknown) = adapter;

    await api.get('/x');

    expect(mockedRefreshSession).toHaveBeenCalledWith('REFRESH');
    expect(mockedWriteRefresh).toHaveBeenCalledWith('NEW_REFRESH');
    expect(setAccessToken).toHaveBeenCalledWith('NEW_ACCESS');
    expect(adapter).toHaveBeenCalledTimes(2); // retry произошёл
  });

  it('on refresh failure clears state and rejects', async () => {
    mockedReadRefresh.mockReturnValue('REFRESH');
    mockedRefreshSession.mockRejectedValue(new Error('refresh exploded'));
    const clear = vi.fn();
    mockedUseAuthStore.getState.mockReturnValue({
      accessToken: 'OLD', user: null,
      setSession: vi.fn(), setAccessToken: vi.fn(), clear,
    });

    const adapter = vi.fn().mockImplementation((config) => {
      const err = new Error('401') as Error & { response: { status: number; data: unknown; headers: {}; config: typeof config } };
      err.response = { status: 401, data: null, headers: {}, config };
      return Promise.reject(err);
    });
    (api.defaults.adapter as unknown) = adapter;

    await expect(api.get('/x')).rejects.toThrow();
    expect(mockedClearRefresh).toHaveBeenCalled();
    expect(clear).toHaveBeenCalled();
  });

  it('concurrent 401s share one refresh call', async () => {
    mockedReadRefresh.mockReturnValue('REFRESH');
    mockedRefreshSession.mockResolvedValue({
      accessToken: 'NEW',
      refreshToken: 'NEW_R',
      userId: 1, email: 'a@b.c', role: 'USER',
    });
    mockedUseAuthStore.getState.mockReturnValue({
      accessToken: 'OLD', user: null,
      setSession: vi.fn(), setAccessToken: vi.fn(), clear: vi.fn(),
    });

    const adapter = vi.fn().mockImplementation((config) => {
      const err = new Error('401') as Error & { response: { status: number; data: unknown; headers: {}; config: typeof config } };
      err.response = { status: 401, data: null, headers: {}, config };
      return Promise.reject(err);
    });
    (api.defaults.adapter as unknown) = adapter;

    await Promise.all([api.get('/a').catch(() => undefined), api.get('/b').catch(() => undefined)]);

    // Один refresh-вызов на оба запроса
    expect(mockedRefreshSession).toHaveBeenCalledTimes(1);
    // Оба запроса ретрайнуты (после первого 401 каждый)
    expect(adapter).toHaveBeenCalledTimes(4); // 2 исходных + 2 retry
  });
});
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `cd frontend && npx vitest run src/__tests__/client.interceptor.test.ts`
Expected: FAIL — module not found.

- [ ] **Step 3: Write auth.ts (refreshSession)**

Write `frontend/src/api/auth.ts`:

```ts
import { api } from './client';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  email: string;
  role: string;
}

export interface RegisterResponse {
  id: number;
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/auth/login', { email, password });
  return data;
}

export async function signup(email: string, password: string): Promise<RegisterResponse> {
  const { data } = await api.post<RegisterResponse>('/auth/signup', { email, password });
  return data;
}

export async function refreshSession(refreshToken: string): Promise<LoginResponse> {
  const { data } = await api.post<LoginResponse>('/auth/refresh', { refreshToken });
  return data;
}
```

- [ ] **Step 4: Write client.ts with interceptor**

Write `frontend/src/api/client.ts`:

```ts
import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import { useAuthStore, readRefresh, writeRefresh, clearRefresh } from '@/auth/store';
import { refreshSession, LoginResponse } from './auth';

export const api: AxiosInstance = axios.create({
  baseURL: '',
  headers: { 'Content-Type': 'application/json' },
});

// ---- request: attach Authorization ----
api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`);
  }
  return config;
});

// ---- response: 401 → refresh + retry ----
let refreshInFlight: Promise<LoginResponse | null> | null = null;

async function performRefresh(): Promise<LoginResponse | null> {
  const refresh = readRefresh();
  if (!refresh) return null;

  try {
    const res = await refreshSession(refresh);
    writeRefresh(res.refreshToken);
    useAuthStore.getState().setAccessToken(res.accessToken);
    useAuthStore.getState().setSession({
      accessToken: res.accessToken,
      user: { id: res.userId, email: res.email, role: res.role },
    });
    return res;
  } catch (e) {
    return null;
  }
}

export async function attemptRefresh(): Promise<boolean> {
  if (!refreshInFlight) {
    refreshInFlight = performRefresh().finally(() => {
      refreshInFlight = null;
    });
  }
  const res = await refreshInFlight;
  return res !== null;
}

export function logoutAndRedirect(): void {
  clearRefresh();
  useAuthStore.getState().clear();
  // гард: если уже на /login — не петлим
  if (!window.location.pathname.startsWith('/login')) {
    window.location.assign('/login');
  }
}

api.interceptors.response.use(
  (r) => r,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    if (error.response?.status !== 401 || !original || original._retry) {
      return Promise.reject(error);
    }
    original._retry = true;

    const ok = await attemptRefresh();
    if (!ok) {
      logoutAndRedirect();
      return Promise.reject(error);
    }

    // обновить Authorization на повторе
    const newToken = useAuthStore.getState().accessToken;
    if (newToken) {
      original.headers.set('Authorization', `Bearer ${newToken}`);
    }
    return api(original);
  },
);
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `cd frontend && npx vitest run src/__tests__/client.interceptor.test.ts`
Expected: PASS, 3 теста зелёные.

- [ ] **Step 6: Run type check**

Run: `cd frontend && npx tsc --noEmit`
Expected: без ошибок.

- [ ] **Step 7: Commit**

```bash
git add frontend/src/api/client.ts frontend/src/api/auth.ts frontend/src/__tests__/client.interceptor.test.ts
git commit -m "feat (Frontend): axios client с 401 refresh interceptor и очередью waiter-ов

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 9: Frontend — AuthBootstrapper + ProtectedRoute

**Files:**
- Create: `frontend/src/auth/AuthBootstrapper.tsx`
- Create: `frontend/src/auth/ProtectedRoute.tsx`
- Create: `frontend/src/__tests__/ProtectedRoute.test.tsx`

**Context:** AuthBootstrapper при старте SPA пытается восстановить сессию через `/auth/refresh`. ProtectedRoute редиректит на `/login`, если `isAuthenticated === false`.

**Interfaces:**
- `AuthBootstrapper({ children }: { children: ReactNode }): JSX.Element` — показывает spinner пока `bootStatus === 'loading'`, иначе рендерит children.
- `ProtectedRoute({ children }: { children: ReactNode }): JSX.Element` — если нет access, редирект на `/login`.

- [ ] **Step 1: Write AuthBootstrapper**

Write `frontend/src/auth/AuthBootstrapper.tsx`:

```tsx
import { useEffect, useState, ReactNode } from 'react';
import { readRefresh, writeRefresh, useAuthStore } from './store';
import { refreshSession } from '@/api/auth';
import { scheduleRefresh } from './refreshScheduler';
import { parseExp } from '@/lib/jwt';

export function AuthBootstrapper({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);

  useEffect(() => {
    const refresh = readRefresh();
    if (!refresh) {
      setReady(true);
      return;
    }

    refreshSession(refresh)
      .then((res) => {
        writeRefresh(res.refreshToken);
        useAuthStore.getState().setSession({
          accessToken: res.accessToken,
          user: { id: res.userId, email: res.email, role: res.role },
        });
        const exp = parseExp(res.accessToken);
        if (exp !== null) {
          scheduleRefresh(exp, () => {
            // no-op: 401 interceptor сам разберётся; это best-effort
            readRefresh();
          });
        }
      })
      .catch(() => {
        // refresh битый — чистим, остаёмся на /login
        useAuthStore.getState().clear();
        writeRefresh; // touch to keep linter happy when nothing to do
      })
      .finally(() => setReady(true));
  }, []);

  if (!ready) {
    return (
      <div className="flex h-screen items-center justify-center text-sm text-gray-500">
        Loading…
      </div>
    );
  }

  return <>{children}</>;
}
```

- [ ] **Step 2: Write ProtectedRoute**

Write `frontend/src/auth/ProtectedRoute.tsx`:

```tsx
import { Navigate, useLocation } from 'react-router-dom';
import { ReactNode } from 'react';
import { useAuth } from './useAuth';

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }
  return <>{children}</>;
}
```

- [ ] **Step 3: Write failing test for ProtectedRoute**

Write `frontend/src/__tests__/ProtectedRoute.test.tsx`:

```tsx
import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { ProtectedRoute } from '@/auth/ProtectedRoute';
import { useAuth } from '@/auth/useAuth';

vi.mock('@/auth/useAuth');

const mockedUseAuth = vi.mocked(useAuth);

describe('ProtectedRoute', () => {
  it('redirects to /login when not authenticated', () => {
    mockedUseAuth.mockReturnValue({
      isAuthenticated: false,
      accessToken: null,
      user: null,
      setSession: vi.fn(),
      setAccessToken: vi.fn(),
      clear: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route path="/" element={<ProtectedRoute><div>Secret</div></ProtectedRoute>} />
        </Routes>
      </MemoryRouter>,
    );

    expect(screen.getByText('Login Page')).toBeInTheDocument();
    expect(screen.queryByText('Secret')).not.toBeInTheDocument();
  });

  it('renders children when authenticated', () => {
    mockedUseAuth.mockReturnValue({
      isAuthenticated: true,
      accessToken: 'A',
      user: { id: 1, email: 'a@b.c', role: 'USER' },
      setSession: vi.fn(),
      setAccessToken: vi.fn(),
      clear: vi.fn(),
    });

    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route path="/login" element={<div>Login Page</div>} />
          <Route path="/" element={<ProtectedRoute><div>Secret</div></ProtectedRoute>} />
        </Routes>
      </MemoryRouter>,
    );

    expect(screen.getByText('Secret')).toBeInTheDocument();
    expect(screen.queryByText('Login Page')).not.toBeInTheDocument();
  });
});
```

- [ ] **Step 4: Run tests**

Run: `cd frontend && npx vitest run src/__tests__/ProtectedRoute.test.tsx`
Expected: PASS.

- [ ] **Step 5: Type check**

Run: `cd frontend && npx tsc --noEmit`
Expected: без ошибок типов.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/auth/AuthBootstrapper.tsx frontend/src/auth/ProtectedRoute.tsx frontend/src/__tests__/ProtectedRoute.test.tsx
git commit -m "feat (Frontend): AuthBootstrapper восстанавливает сессию, ProtectedRoute гардит

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 10: Frontend — shadcn UI primitives

**Files:**
- Create: `frontend/src/components/ui/button.tsx`
- Create: `frontend/src/components/ui/input.tsx`
- Create: `frontend/src/components/ui/label.tsx`
- Create: `frontend/src/components/ui/card.tsx`

**Context:** Минимальный набор из shadcn/ui. Копируем компоненты (не ставим как зависимость) — это и есть «shadcn-way». Без тестов (UI-примитивы).

- [ ] **Step 1: Write Button**

Write `frontend/src/components/ui/button.tsx`:

```tsx
import { forwardRef, ButtonHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

type Variant = 'primary' | 'secondary';

interface Props extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: Variant;
  isLoading?: boolean;
}

const base =
  'inline-flex items-center justify-center rounded-md text-sm font-medium transition-colors ' +
  'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2 ' +
  'disabled:pointer-events-none disabled:opacity-50 h-10 px-4';

const variants: Record<Variant, string> = {
  primary: 'bg-blue-600 text-white hover:bg-blue-700',
  secondary: 'bg-white text-gray-900 border border-gray-300 hover:bg-gray-50',
};

export const Button = forwardRef<HTMLButtonElement, Props>(
  ({ className, variant = 'primary', isLoading, disabled, children, ...rest }, ref) => (
    <button
      ref={ref}
      className={cn(base, variants[variant], className)}
      disabled={disabled || isLoading}
      {...rest}
    >
      {isLoading ? '…' : children}
    </button>
  ),
);
Button.displayName = 'Button';
```

- [ ] **Step 2: Write Input**

Write `frontend/src/components/ui/input.tsx`:

```tsx
import { forwardRef, InputHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

export const Input = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  ({ className, ...rest }, ref) => (
    <input
      ref={ref}
      className={cn(
        'flex h-10 w-full rounded-md border border-gray-300 bg-white px-3 py-2 text-sm',
        'placeholder:text-gray-400',
        'focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500',
        'disabled:cursor-not-allowed disabled:opacity-50',
        className,
      )}
      {...rest}
    />
  ),
);
Input.displayName = 'Input';
```

- [ ] **Step 3: Write Label**

Write `frontend/src/components/ui/label.tsx`:

```tsx
import { forwardRef, LabelHTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

export const Label = forwardRef<HTMLLabelElement, LabelHTMLAttributes<HTMLLabelElement>>(
  ({ className, ...rest }, ref) => (
    <label ref={ref} className={cn('text-sm font-medium text-gray-700', className)} {...rest} />
  ),
);
Label.displayName = 'Label';
```

- [ ] **Step 4: Write Card**

Write `frontend/src/components/ui/card.tsx`:

```tsx
import { HTMLAttributes } from 'react';
import { cn } from '@/lib/utils';

export function Card({ className, ...rest }: HTMLAttributes<HTMLDivElement>) {
  return (
    <div
      className={cn('rounded-lg border border-gray-200 bg-white p-8 shadow-sm', className)}
      {...rest}
    />
  );
}
```

- [ ] **Step 5: Type check**

Run: `cd frontend && npx tsc --noEmit`
Expected: без ошибок.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/components/ui
git commit -m "feat (Frontend): shadcn-style UI primitives (Button, Input, Label, Card)

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 11: Frontend — AuthForm + AuthLayout

**Files:**
- Create: `frontend/src/components/AuthLayout.tsx`
- Create: `frontend/src/components/AuthForm.tsx`
- Create: `frontend/src/__tests__/AuthForm.test.tsx`

**Context:** `AuthLayout` — рамка с логотипом и карточкой по центру. `AuthForm` — переиспользуемая форма для login и signup. Управляет состоянием, валидацией через zod, маппит ошибки API на поля.

**Interfaces:**
- `AuthForm({ mode, onSubmit, defaultEmail? }: Props): JSX.Element`
  - `mode: 'login' | 'signup'`
  - `onSubmit({ email, password }): Promise<{ fieldErrors?: Record<string, string>; formError?: string }>` — возвращает структурированные ошибки
  - `defaultEmail?: string` — префилл (для redirect signup → login)
- `AuthLayout({ title, subtitle, children }: Props): JSX.Element`

- [ ] **Step 1: Write AuthLayout**

Write `frontend/src/components/AuthLayout.tsx`:

```tsx
import { ReactNode } from 'react';

export function AuthLayout({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle?: string;
  children: ReactNode;
}) {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center bg-gray-50 px-4">
      <div className="mb-6 text-xl font-semibold text-gray-900">InterviewBeater</div>
      <div className="w-full max-w-sm rounded-lg border border-gray-200 bg-white p-8 shadow-sm">
        <h1 className="mb-1 text-2xl font-semibold text-gray-900">{title}</h1>
        {subtitle && <p className="mb-6 text-sm text-gray-500">{subtitle}</p>}
        {children}
      </div>
    </div>
  );
}
```

- [ ] **Step 2: Write AuthForm**

Write `frontend/src/components/AuthForm.tsx`:

```tsx
import { useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Button } from './ui/button';
import { Input } from './ui/input';
import { Label } from './ui/label';

const schema = z.object({
  email: z.string().email('Enter a valid email'),
  password: z.string().min(8, 'At least 8 characters'),
});

type FormValues = z.infer<typeof schema>;

export type SubmitResult =
  | { ok: true }
  | { ok: false; fieldErrors?: Partial<Record<keyof FormValues, string>>; formError?: string };

interface Props {
  mode: 'login' | 'signup';
  defaultEmail?: string;
  onSubmit: (values: FormValues) => Promise<SubmitResult>;
}

export function AuthForm({ mode, defaultEmail = '', onSubmit }: Props) {
  const [formError, setFormError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { email: defaultEmail, password: '' },
  });

  async function submit(values: FormValues) {
    setFormError(null);
    const result = await onSubmit(values);
    if (result.ok) return;

    if (result.fieldErrors) {
      for (const [field, message] of Object.entries(result.fieldErrors)) {
        if (message) setError(field as keyof FormValues, { message });
      }
    }
    if (result.formError) setFormError(result.formError);
  }

  return (
    <form className="space-y-4" onSubmit={handleSubmit(submit)} noValidate>
      <div className="space-y-1">
        <Label htmlFor="email">Email</Label>
        <Input id="email" type="email" autoComplete="email" {...register('email')} />
        {errors.email && <p className="text-sm text-red-600">{errors.email.message}</p>}
      </div>

      <div className="space-y-1">
        <Label htmlFor="password">Password</Label>
        <Input
          id="password"
          type="password"
          autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
          {...register('password')}
        />
        {errors.password && <p className="text-sm text-red-600">{errors.password.message}</p>}
      </div>

      {formError && (
        <p className="text-sm text-red-600" role="alert">{formError}</p>
      )}

      <Button type="submit" isLoading={isSubmitting} className="w-full">
        Continue
      </Button>
    </form>
  );
}
```

- [ ] **Step 3: Write failing tests**

Write `frontend/src/__tests__/AuthForm.test.tsx`:

```tsx
import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { AuthForm } from '@/components/AuthForm';

describe('AuthForm', () => {
  it('shows field validation errors', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn();
    render(<AuthForm mode="login" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'not-an-email');
    await user.type(screen.getByLabelText(/password/i), 'short');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByText(/valid email/i)).toBeInTheDocument();
    expect(screen.getByText(/at least 8/i)).toBeInTheDocument();
    expect(onSubmit).not.toHaveBeenCalled();
  });

  it('calls onSubmit with parsed values on valid input', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue({ ok: true });
    render(<AuthForm mode="login" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'a@b.c');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(onSubmit).toHaveBeenCalledWith({ email: 'a@b.c', password: 'password123' });
  });

  it('maps server fieldErrors onto fields', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue({
      ok: false,
      fieldErrors: { email: 'Email already registered' },
    });
    render(<AuthForm mode="signup" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'a@b.c');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByText(/already registered/i)).toBeInTheDocument();
  });

  it('shows formError from server', async () => {
    const user = userEvent.setup();
    const onSubmit = vi.fn().mockResolvedValue({
      ok: false,
      formError: 'Invalid email or password',
    });
    render(<AuthForm mode="login" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'a@b.c');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByRole('alert')).toHaveTextContent(/invalid email or password/i);
  });
});

describe('AuthForm (signup → 409)', () => {
  it('signup with duplicate email surfaces server error on email field', async () => {
    const user = userEvent.setup();
    // Симулируем ответ бэка после правки ControllerAdvice: 409 → фронт мапит в fieldErrors.email
    const onSubmit = vi.fn().mockImplementation(async () => {
      return { ok: false, fieldErrors: { email: 'Email already registered' } } as const;
    });
    render(<AuthForm mode="signup" onSubmit={onSubmit} />);

    await user.type(screen.getByLabelText(/email/i), 'a@b.c');
    await user.type(screen.getByLabelText(/password/i), 'password123');
    await user.click(screen.getByRole('button', { name: /continue/i }));

    expect(await screen.findByText(/already registered/i)).toBeInTheDocument();
  });
});
```

- [ ] **Step 4: Run tests**

Run: `cd frontend && npx vitest run src/__tests__/AuthForm.test.tsx`
Expected: PASS, 4 теста зелёные.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/components/AuthLayout.tsx frontend/src/components/AuthForm.tsx frontend/src/__tests__/AuthForm.test.tsx
git commit -m "feat (Frontend): AuthForm с zod-валидацией и маппингом серверных ошибок

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 12: Frontend — LoginPage, SignupPage, LogoutButton, HomePage, App.tsx, main.tsx

**Files:**
- Create: `frontend/src/components/LogoutButton.tsx`
- Create: `frontend/src/pages/LoginPage.tsx`
- Create: `frontend/src/pages/SignupPage.tsx`
- Create: `frontend/src/pages/HomePage.tsx`
- Create: `frontend/src/api/questions.ts`
- Create: `frontend/src/App.tsx`
- Create: `frontend/src/main.tsx`

**Context:** Страницы, которые используют всё, что мы сделали выше. `App.tsx` настраивает роутер. `main.tsx` — точка входа.

**Interfaces:**
- `LoginPage`, `SignupPage`, `HomePage` — компоненты-страницы.
- `listQuestions(): Promise<Question[]>` — из `api/questions.ts`.

- [ ] **Step 1: Write api/questions.ts**

Write `frontend/src/api/questions.ts`:

```ts
import { api } from './client';

export interface Question {
  id: number;
  title: string;
}

export async function listQuestions(): Promise<Question[]> {
  const { data } = await api.get<Question[]>('/questions');
  return data;
}
```

- [ ] **Step 2: Write LogoutButton**

Write `frontend/src/components/LogoutButton.tsx`:

```tsx
import { useNavigate } from 'react-router-dom';
import { Button } from './ui/button';
import { logoutAndRedirect } from '@/api/client';

export function LogoutButton() {
  const navigate = useNavigate();

  function handleClick() {
    logoutAndRedirect();
    navigate('/login');
  }

  return (
    <Button variant="secondary" onClick={handleClick}>
      Log out
    </Button>
  );
}
```

- [ ] **Step 3: Write LoginPage**

Write `frontend/src/pages/LoginPage.tsx`:

```tsx
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { AuthLayout } from '@/components/AuthLayout';
import { AuthForm, SubmitResult } from '@/components/AuthForm';
import { login } from '@/api/auth';
import { useAuth } from '@/auth/useAuth';
import { writeRefresh } from '@/auth/store';
import { scheduleRefresh } from '@/auth/refreshScheduler';
import { parseExp } from '@/lib/jwt';
import { AxiosError } from 'axios';

export function LoginPage() {
  const { setSession } = useAuth();
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const defaultEmail = params.get('email') ?? '';

  async function onSubmit({ email, password }: { email: string; password: string }): Promise<SubmitResult> {
    try {
      const res = await login(email, password);
      writeRefresh(res.refreshToken);
      setSession({ accessToken: res.accessToken, user: { id: res.userId, email: res.email, role: res.role } });
      const exp = parseExp(res.accessToken);
      if (exp !== null) {
        scheduleRefresh(exp, () => { /* best-effort: 401 fallback covers it */ });
      }
      navigate('/', { replace: true });
      return { ok: true };
    } catch (e) {
      if (e instanceof AxiosError && e.response?.status === 401) {
        return { ok: false, formError: 'Invalid email or password' };
      }
      return { ok: false, formError: 'Network error. Try again.' };
    }
  }

  return (
    <AuthLayout title="Log in" subtitle="Welcome back.">
      <AuthForm mode="login" onSubmit={onSubmit} defaultEmail={defaultEmail} />
      <p className="mt-6 text-center text-sm text-gray-600">
        Don&apos;t have an account?{' '}
        <Link to="/signup" className="font-medium text-blue-600 hover:underline">Sign up</Link>
      </p>
    </AuthLayout>
  );
}
```

- [ ] **Step 4: Write SignupPage**

Write `frontend/src/pages/SignupPage.tsx`:

```tsx
import { Link, useNavigate } from 'react-router-dom';
import { AxiosError } from 'axios';
import { AuthLayout } from '@/components/AuthLayout';
import { AuthForm, SubmitResult } from '@/components/AuthForm';
import { signup } from '@/api/auth';

export function SignupPage() {
  const navigate = useNavigate();

  async function onSubmit({ email, password }: { email: string; password: string }): Promise<SubmitResult> {
    try {
      await signup(email, password);
      navigate(`/login?email=${encodeURIComponent(email)}`, { replace: true });
      return { ok: true };
    } catch (e) {
      if (e instanceof AxiosError) {
        if (e.response?.status === 409) {
          return { ok: false, fieldErrors: { email: 'Email already registered' } };
        }
        if (e.response?.status === 400 && e.response.data?.fields) {
          return { ok: false, fieldErrors: mapServerFields(e.response.data.fields) };
        }
      }
      return { ok: false, formError: 'Sign up failed. Try again.' };
    }
  }

  return (
    <AuthLayout title="Sign up" subtitle="Create your InterviewBeater account.">
      <AuthForm mode="signup" onSubmit={onSubmit} />
      <p className="mt-6 text-center text-sm text-gray-600">
        Already have an account?{' '}
        <Link to="/login" className="font-medium text-blue-600 hover:underline">Log in</Link>
      </p>
    </AuthLayout>
  );
}

function mapServerFields(fields: Array<{ field: string; message: string }>): Record<string, string> {
  const out: Record<string, string> = {};
  for (const f of fields) out[f.field] = f.message;
  return out;
}
```

- [ ] **Step 5: Write HomePage**

Write `frontend/src/pages/HomePage.tsx`:

```tsx
import { useEffect, useState } from 'react';
import { listQuestions, Question } from '@/api/questions';
import { LogoutButton } from '@/components/LogoutButton';
import { useAuth } from '@/auth/useAuth';

export function HomePage() {
  const { user } = useAuth();
  const [questions, setQuestions] = useState<Question[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listQuestions()
      .then(setQuestions)
      .catch(() => setError('Failed to load questions.'));
  }, []);

  return (
    <div className="mx-auto max-w-2xl px-4 py-10">
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Questions</h1>
          {user && <p className="text-sm text-gray-500">Signed in as {user.email}</p>}
        </div>
        <LogoutButton />
      </div>

      {error && <p className="text-sm text-red-600" role="alert">{error}</p>}

      {questions === null && !error && (
        <p className="text-sm text-gray-500">Loading…</p>
      )}

      {questions && questions.length === 0 && (
        <p className="text-sm text-gray-500">No questions yet.</p>
      )}

      {questions && questions.length > 0 && (
        <ul className="divide-y divide-gray-200 rounded-md border border-gray-200 bg-white">
          {questions.map((q) => (
            <li key={q.id} className="p-4">
              <p className="text-sm text-gray-500">#{q.id}</p>
              <p className="text-gray-900">{q.title}</p>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
```

- [ ] **Step 6: Write App.tsx and main.tsx**

Write `frontend/src/App.tsx`:

```tsx
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthBootstrapper } from '@/auth/AuthBootstrapper';
import { ProtectedRoute } from '@/auth/ProtectedRoute';
import { LoginPage } from '@/pages/LoginPage';
import { SignupPage } from '@/pages/SignupPage';
import { HomePage } from '@/pages/HomePage';

export function App() {
  return (
    <BrowserRouter>
      <AuthBootstrapper>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <HomePage />
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<LoginPage />} />
        </Routes>
      </AuthBootstrapper>
    </BrowserRouter>
  );
}
```

Write `frontend/src/main.tsx`:

```tsx
import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App';
import './index.css';

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>,
);
```

- [ ] **Step 7: Type check**

Run: `cd frontend && npx tsc --noEmit`
Expected: без ошибок.

- [ ] **Step 8: Commit**

```bash
git add frontend/src/api/questions.ts frontend/src/components/LogoutButton.tsx frontend/src/pages frontend/src/App.tsx frontend/src/main.tsx
git commit -m "feat (Frontend): страницы Login/Signup/Home + App + main, всё вместе

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 13: Final — type check, all tests, manual smoke

**Files:** none (verification only).

- [ ] **Step 1: Run all frontend tests**

Run:
```bash
cd frontend
npx vitest run
```
Expected: все тесты зелёные.

- [ ] **Step 2: Type check frontend**

Run: `cd frontend && npx tsc --noEmit`
Expected: без ошибок.

- [ ] **Step 3: Production build**

Run: `cd frontend && npm run build`
Expected: `dist/` создаётся, без ошибок TS и без warnings о нерезолвленных импортах.

- [ ] **Step 4: Backend tests**

Run: `./mvnw -q test`
Expected: все бэк-тесты (включая новый `ControllerAdviceTest`) зелёные.

- [ ] **Step 5: Manual smoke (developer)**

Start backend:
```bash
./mvnw -q spring-boot:run
```

In another terminal, start frontend:
```bash
cd frontend && npm run dev
```

Visit http://localhost:5173. Verify:
- [ ] Redirect → /login.
- [ ] Sign up нового email → редирект на /login с префиллом.
- [ ] Log in → redirect на /, виден список questions из БД.
- [ ] F5 на / → сессия восстанавливается, список загружается снова.
- [ ] Sign up того же email → "Email already registered".
- [ ] Log out → redirect на /login, refresh в localStorage удалён.

- [ ] **Step 6: Final commit (если были фиксы)**

Если в шаге 5 пришлось что-то править:
```bash
git add -A
git commit -m "fix (Frontend): smoke-test фиксы

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

(Если правок не было — коммит не нужен, задача просто закрывается.)

- [ ] **Step 7: Done**

Спек `docs/superpowers/specs/2026-09-29-frontend-auth-design.md` реализован. Критерии приёмки выполнены.
