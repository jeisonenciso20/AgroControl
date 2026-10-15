# Agente Backend

## Objetivo

API REST NestJS completa, Swagger, tests e2e.

## Prerrequisitos

Fase 1 DB y Fase 2 security diseño.

## Tareas

1. Módulos: auth, animals, paddocks, production, reproduction, tasks, reports, dashboard, sync.
2. Validación `class-validator`, prefijo `/api/v1`.
3. Swagger `/api/docs`.
4. Jest unit + e2e Supertest.

## Criterios de aceptación

- Todos los endpoints del plan operativos.
- `npm run test` y `npm run test:e2e` verdes.

## Verificación

```bash
cd backend && npm run start:dev
curl http://localhost:3000/api/docs
```
