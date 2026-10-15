# Estrategia de pruebas

## Backend

| Capa | Herramienta | Comando | Notas |
|------|-------------|---------|-------|
| Unit | Jest + mocks Prisma | `npm run test` | Servicios aislados |
| E2E HTTP (mock) | Supertest + app Nest sin Postgres | `npm run test:e2e` | ~35 tests, todas las rutas |
| Smoke live | Supertest contra API real | `npm run test:e2e:live` | Requiere Docker + `start:dev` |
| CRUD live | Postgres real | `npm run test:e2e:crud` | Escritura/lectura CRUD + sync |

Archivos clave:

- [`backend/test/endpoints.e2e-spec.ts`](../../backend/test/endpoints.e2e-spec.ts)
- [`backend/test/smoke-live.e2e-spec.ts`](../../backend/test/smoke-live.e2e-spec.ts)
- [`backend/test/crud-live.e2e-spec.ts`](../../backend/test/crud-live.e2e-spec.ts)

Preflight migraciones: `npm run preflight:live`.

**Presentación:** Swagger demuestra la API en vivo; `npm run test:e2e` demuestra cobertura automatizada reproducible.

## Android

- Unit: validadores, mappers (p. ej. pendientes, fechas).
- Manual / smoke: [`docs/architecture/e2e-smoke.md`](../architecture/e2e-smoke.md).
- Instrumented Room/sync: opcional, no bloqueante para entrega v1.

## Evidencia recomendada en informe

1. Captura terminal `Tests: … passed` tras `npm run test:e2e`.
2. Captura Swagger con respuesta 200 en `/dashboard/summary` o `/auth/me`.
3. (Opcional) salida `test:e2e:crud` con Postgres.
