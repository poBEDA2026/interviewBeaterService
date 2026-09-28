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
