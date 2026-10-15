# AgroControl — Roadmap global

Orden de ejecución: **Fase 0 → 6**. Cada agente tiene su checklist en `docs/agents/<rol>/todo.md`.

| Fase | Agente | Estado |
|------|--------|--------|
| 0 | Todos | Documentación y scaffolding monorepo |
| 1 | [database](docs/agents/database/todo.md) | PostgreSQL Docker + Prisma |
| 2 | [security](docs/agents/security/todo.md) | JWT, hardening, docs |
| 3 | [backend](docs/agents/backend/todo.md) | API NestJS + tests |
| 4 | [persistence](docs/agents/persistence/todo.md) | Room sync + WorkManager |
| 5 | [frontend](docs/agents/frontend/todo.md) | Pantallas Figma + Retrofit |
| 6 | Integración | Smoke E2E + README |

## Dependencias

```text
DB → Backend → Persistence → Frontend
Security (diseño) → Backend (auth)
```

## MVP checklist

- [x] `docker compose up` levanta PostgreSQL
- [x] `npm run test:e2e` en backend (ejecutar localmente tras `npm install`)
- [x] Login Android + dashboard con KPIs
- [x] CRUD animales offline + sync (potreros vía pull)
- [x] Producción, reportes, pendientes (vista + API)
