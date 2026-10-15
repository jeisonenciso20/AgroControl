# Smoke test E2E (manual)

## Backend

1. `docker compose -f infra/docker-compose.yml up -d`
2. `cd backend && cp .env.example .env && npm install`
3. `npm run db:setup`
4. `npm run start:dev`
5. Abrir http://localhost:3000/api/docs
6. `npm run test && npm run test:e2e`

Opcional live: `npm run test:e2e:live` y `npm run test:e2e:crud` (API + Postgres en marcha).

## Swagger (demostración API)

1. `POST /auth/login` con `demo@agrocontrol.local` / `demo1234`
2. Authorize con `accessToken`
3. `GET /auth/me`, `GET /dashboard/summary`, `GET /sync/pull`

Ver guion completo: [`docs/entrega/BACKEND-API.md`](../entrega/BACKEND-API.md).

## Android (emulador)

1. API en host puerto 3000.
2. App usa `http://10.0.2.2:3000/api/v1/`.
3. Login demo → KPIs en Inicio, lista animales, sync en **Más**.
4. Icono perfil en Inicio → pantalla **Perfil** (nombre, email, finca) vía `auth/me`.
5. Reportes semanal/mensual con sesión activa.

## Aislamiento por cuenta

1. Login **demo** → ver animales/potreros semilla.
2. Cerrar sesión → **registrar usuario nuevo** → Inicio vacío (sin datos ajenos).
3. Swagger con token del usuario nuevo → listas vacías coherentes con la app.

## Pendientes y tareas

1. Registrar trabajo de potrero (fecha hoy) → aparece en Inicio / Pendientes.
2. Marcar **check** en tarea → desaparece de pendientes; en servidor `status=COMPLETED` (`PATCH /tasks/:id/complete` o sync).

## Offline

1. Agregar animal sin red → aparece en lista.
2. Activar red → sincronizar desde **Más** (log `sync/push` / pull).

Más contexto app: [`docs/entrega/ANDROID-APP.md`](../entrega/ANDROID-APP.md).
