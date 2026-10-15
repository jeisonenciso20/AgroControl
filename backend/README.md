# AgroControl Backend

NestJS API + Prisma + PostgreSQL.

Informe de entrega (BD, endpoints, pruebas, Swagger/Postman): [`docs/entrega/BACKEND-API.md`](../docs/entrega/BACKEND-API.md).

## Setup

```bash
docker compose -f ../infra/docker-compose.yml up -d
cp .env.example .env
npm install
npm run db:setup
npm run start:dev
```

`db:setup` ejecuta en orden: `prisma generate` → `migrate deploy` (crea tablas) → `db seed` (datos demo). No ejecutes solo `db seed` sin migrar antes.

Swagger: http://localhost:3000/api/docs

OpenAPI en archivo (con API en marcha): `npm run openapi:export` → `openapi/agrocontrol-openapi.json`. Colección Postman: [`collections/README.md`](collections/README.md).

Demo user: `demo@agrocontrol.local` / `demo1234`

Los datos de **semilla** (animales, potreros, producción, tareas demo) pertenecen solo a la finca del usuario demo (`seed-finca-1`). Un usuario **registrado nuevo** obtiene una finca vacía en Postgres; la app debe limpiar Room al cambiar de cuenta para no mezclar caché local.

## Probar API con Swagger

Swagger sirve para probar la misma API que usa la app Android, desde el navegador en tu PC.

1. Levanta el backend: `npm run start:dev`.
2. Abre **http://localhost:3000/api/docs** (en el PC, no en el emulador).
3. **Login:** sección **auth** → `POST /api/v1/auth/login` → *Try it out* → body:
   ```json
   { "email": "demo@agrocontrol.local", "password": "demo1234" }
   ```
   Copia `accessToken` de la respuesta.
4. **Authorize:** botón **Authorize** (candado arriba) → pega el token (solo el JWT, sin la palabra Bearer si Swagger ya la añade) → *Authorize*.
5. Prueba endpoints que alimentan la app:

   | Swagger | Pantalla Android |
   |---------|------------------|
   | `GET /dashboard/summary` | Inicio (KPIs) |
   | `GET /sync/pull`, `POST /sync/push` | Sync / listas locales (animales, potreros, producción) |
   | `GET /reports/weekly`, `GET /reports/monthly` | Reportes (Más → Reportes) |
   | `GET /tasks` | Pendientes (también vienen en pull) |

6. **Emulador:** la app llama a `http://10.0.2.2:3000/api/v1/` (host del PC visto desde el emulador). Swagger usa `localhost:3000`; es la misma API.
7. **Errores frecuentes:** `401` → token expirado (vuelve a login o usa `POST /auth/refresh`); `400` → revisa body/query frente al esquema en Swagger; CORS no afecta al emulador nativo.

## Tests

### Unitarios (servicios)

```bash
npm run test
```

### E2E con mocks (sin Postgres)

Valida que **cada ruta HTTP** responde (auth, dashboard, CRUD, sync, reports). No requiere Docker.

```bash
npm run test:e2e
```

Archivos: [`test/endpoints.e2e-spec.ts`](test/endpoints.e2e-spec.ts), harness en [`test/helpers/create-test-app.ts`](test/helpers/create-test-app.ts).

### Smoke E2E contra servidor real (Postgres + API en marcha)

1. `docker compose -f ../infra/docker-compose.yml up -d`
2. `npm run db:setup`
3. En otra terminal: `npm run start:dev`
4. Ejecutar:

```bash
npm run test:e2e:live
npm run test:e2e:crud
```

`test:e2e:crud` ejecuta CRUD completo (animals, paddocks, production, reproduction, tasks) contra Postgres y verifica lectura tras cada escritura.

Opcional: `API_BASE=http://127.0.0.1:3000` si el puerto cambia.

En Windows sin `cross-env`, puedes usar: `set RUN_LIVE_E2E=1&& npm run test:e2e -- smoke-live`

Antes de los tests live, puedes comprobar migraciones:

```bash
npm run preflight:live
```

### Troubleshooting tests live (500 / P2022)

**Síntoma:** `npm run test:e2e:crud` pasa **animals** pero falla con **500** en paddocks, production, reproduction o tasks.

**Causa:** Postgres sigue en el esquema `init`; falta la migración `20260315120000_crud_fields` (columnas `tipoPasto`, `jornada`, `datosJson`, `workType`, etc.). En la consola de Nest verás `PrismaClientKnownRequestError` **P2022** (*column does not exist*).

**Solución:**

1. Detén `npm run start:dev`.
2. Con Docker Postgres arriba: `npm run db:setup` (o al menos `npm run prisma:deploy` + `npx prisma generate`).
3. `npx prisma migrate status` debe mostrar **2** migraciones aplicadas (`init` + `crud_fields`).
4. Vuelve a `npm run start:dev` y ejecuta `npm run test:e2e:crud`.

No basta con `start:dev` si nunca migraste después de actualizar el código CRUD.
