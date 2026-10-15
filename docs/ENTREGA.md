# Documentación de entrega — AgroControl

Documento principal para **presentación, evaluación y reproducción** del proyecto. Stack: app Android offline-first + API NestJS + PostgreSQL (Docker).

## Resumen ejecutivo

**AgroControl** gestiona animales, potreros, producción (leche/carne), reproducción y trabajos pendientes por **finca**. Cada usuario tiene su finca (multi-tenant vía JWT `fincaId`). La app funciona sin red y sincroniza cambios cuando hay conexión.

| Componente | Tecnología |
|------------|------------|
| Cliente móvil | Kotlin, Jetpack Compose, Room, Retrofit, WorkManager |
| API | NestJS 11, Prisma, JWT |
| Base de datos | PostgreSQL 16 (Docker Compose) |

## Qué se entrega

1. **Backend — API funcional**: base de datos migrada y semilla demo, REST documentada en Swagger, pruebas automatizadas e2e.
2. **App Android**: login/registro, CRUD offline con sync, dashboard, pendientes (completar con check), perfil lectura, etc.
3. **Documentación**: este archivo, anexos en [`docs/entrega/`](entrega/), arquitectura en [`docs/architecture/`](architecture/).

Documentos históricos del diseño inicial: [`Memoria.md`](../Memoria.md), [`todo.md`](../todo.md), [`Design.md`](../Design.md) — **la referencia vigente para entrega es este documento y sus enlaces**.

## Reproducir en ~15 minutos

### 1. Base de datos y API

```bash
docker compose -f infra/docker-compose.yml up -d
cd backend
cp .env.example .env
npm install
npm run db:setup
npm run start:dev
```

- Swagger: **http://localhost:3000/api/docs**
- Adminer (opcional): **http://localhost:8080** — servidor `postgres`, usuario `agro`, BD `agrocontrol`
- Usuario demo: `demo@agrocontrol.local` / `demo1234`

### 2. Demostrar API en Swagger (válido para defensa)

1. `POST /api/v1/auth/login` con el usuario demo → copiar `accessToken`.
2. **Authorize** (candado) → pegar el JWT.
3. Probar, por ejemplo:
   - `GET /api/v1/auth/me`
   - `GET /api/v1/dashboard/summary`
   - `GET /api/v1/sync/pull`
   - `GET /api/v1/animals`

Swagger es **equivalente válido** a Postman/Insomnia para demostración oral. Además existe colección Postman en [`backend/collections/`](../backend/collections/).

### 3. Pruebas automatizadas (evidencia en terminal)

```bash
cd backend
npm run test:e2e
```

Esperado: suite **endpoints** en verde (~35 tests, sin Docker).

Opcional con Postgres y API en marcha:

```bash
npm run test:e2e:live
npm run test:e2e:crud
```

### 4. App Android

Abrir `app/` en Android Studio, ejecutar en emulador. API base: `http://10.0.2.2:3000/api/v1/`. Login demo y revisar Inicio, Animales, sync en **Más**.

Checklist manual: [`docs/architecture/e2e-smoke.md`](architecture/e2e-smoke.md).

## Evidencias sugeridas para slides

| Evidencia | Qué demuestra |
|-----------|----------------|
| **[Guía Word Swagger](entrega/Guia-Pruebas-API-Swagger.docx)** | Captura + contexto de **todos** los endpoints en Swagger UI |
| Captura Swagger + respuesta JSON | API funcional y contrato |
| Terminal `npm run test:e2e` passed | Endpoints probados automáticamente |
| Adminer o diagrama ER | Base de datos poblada |
| App: demo vs usuario registrado nuevo | Aislamiento por finca + Room limpio |
| App: marcar pendiente con check | Estado COMPLETED + sync |

## Guion de presentación (5 minutos)

1. **Arquitectura** — diagrama en [`docs/architecture/overview.md`](architecture/overview.md).
2. **Base de datos** — Docker, migraciones Prisma, semilla solo demo ([`docs/entrega/BACKEND-API.md`](entrega/BACKEND-API.md)).
3. **API** — Swagger: login + 2–3 GET autenticados.
4. **Pruebas** — `npm run test:e2e`.
5. **App** (opcional) — flujo offline y sync.

## Índice de documentos

| Documento | Contenido |
|-----------|-----------|
| [entrega/Guia-Pruebas-API-Swagger.docx](entrega/Guia-Pruebas-API-Swagger.docx) | Guía visual: prueba de cada endpoint en Swagger |
| [entrega/README-GUIA-SWAGGER.md](entrega/README-GUIA-SWAGGER.md) | Cómo regenerar capturas y el Word |
| [entrega/BACKEND-API.md](entrega/BACKEND-API.md) | BD, catálogo de endpoints, pruebas, Swagger, Postman |
| [entrega/ANDROID-APP.md](entrega/ANDROID-APP.md) | Funcionalidades app, emulador, checklist |
| [architecture/overview.md](architecture/overview.md) | Contenedores, sync, multi-tenant |
| [architecture/database.md](architecture/database.md) | ER, comandos, credenciales |
| [patterns/offline-sync.md](patterns/offline-sync.md) | Push/pull, entidades sync |
| [patterns/api-contracts.md](patterns/api-contracts.md) | Convenciones REST y sync |
| [patterns/testing-strategy.md](patterns/testing-strategy.md) | Estrategia de pruebas |
| [backend/README.md](../backend/README.md) | Setup rápido y troubleshooting |
