# Agente Database

## Objetivo

PostgreSQL en Docker, esquema Prisma, migraciones y seeds.

## Prerrequisitos

Docker Desktop instalado.

## Tareas

1. `infra/docker-compose.yml` con PostgreSQL 16 + healthcheck.
2. `backend/prisma/schema.prisma` con todas las entidades.
3. Migración inicial y `prisma/seed.ts`.
4. Documentar en `docs/architecture/database.md`.

## Criterios de aceptación

- `docker compose -f infra/docker-compose.yml up -d` healthy.
- `npx prisma migrate deploy` y `npx prisma db seed` sin error.

## Verificación

```bash
cd backend && npx prisma studio
```
