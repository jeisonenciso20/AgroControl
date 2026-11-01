# AgroControl (monorepo)

Gestión agropecuaria: **Android** (offline-first) + **NestJS** + **PostgreSQL** (Docker).

## Documentación de entrega

**Punto de entrada para evaluación y presentación:** [`docs/ENTREGA.md`](docs/ENTREGA.md)

Incluye guion Swagger, pruebas e2e, enlaces a backend/app y diagramas.

| Anexo | Descripción |
|-------|-------------|
| [`docs/entrega/Guia-Pruebas-API-Swagger.docx`](docs/entrega/Guia-Pruebas-API-Swagger.docx) | Guía Word con capturas Swagger por endpoint |
| [`docs/entrega/BACKEND-API.md`](docs/entrega/BACKEND-API.md) | API funcional, BD, endpoints, Postman |
| [`docs/entrega/ANDROID-APP.md`](docs/entrega/ANDROID-APP.md) | Cliente Android |
| [`docs/architecture/overview.md`](docs/architecture/overview.md) | Arquitectura |

Documentos de diseño e historial: [`docs/design/`](docs/design/), [`Memoria.md`](Memoria.md), [`todo.md`](todo.md) (referencia histórica; entrega vigente en `docs/ENTREGA.md`).

## Estructura

| Carpeta | Descripción |
|---------|-------------|
| `app/` | Cliente Android (Kotlin, Room, Retrofit, Compose) |
| `backend/` | API NestJS + Prisma |
| `infra/` | `docker-compose.yml` PostgreSQL + Adminer |
| `docs/` | Arquitectura, entrega, patrones, diseño UI |

## Inicio rápido

```bash
# Base de datos
docker compose -f infra/docker-compose.yml up -d

# API (PostgreSQL debe estar arriba; migrar ANTES del seed)
cd backend
cp .env.example .env
npm install
npm run db:setup
npm run start:dev
```

### Base de datos en Neon (nube, sin Docker)

`backend/neon.ts` declara la configuración del proyecto Neon y `neon deploy` inyecta
`DATABASE_URL` (pooled) y `DATABASE_URL_UNPOOLED` (directa, para migraciones) en
`backend/.env`. Prisma usa ambas vía `url` + `directUrl`.

```bash
cd backend
neon login      # solo la primera vez
neon deploy
npm run db:setup
npm run start:dev
```

- Swagger: http://localhost:3000/api/docs  
- OpenAPI JSON (con API en marcha): http://localhost:3000/api/docs-json  
- Usuario demo: `demo@agrocontrol.local` / `demo1234`

### Producción (Render)

| Recurso | URL |
|---|---|
| API | https://agrocontrol-apjl.onrender.com/api/v1/ |
| Swagger | https://agrocontrol-apjl.onrender.com/api/docs |

La app Android ya apunta ahí por defecto (`BuildConfig.API_BASE_URL`).

## Android

Abrir `app/` en Android Studio, sincronizar Gradle y ejecutar. La app usa por
defecto el backend en Render (`https://agrocontrol-apjl.onrender.com/api/v1/`).

Para usar un backend local en el emulador:

```bash
./gradlew assembleDebug -PapiBaseUrl=http://10.0.2.2:3000/api/v1/
```

El primer request tras ~15 min de inactividad puede tardar hasta un minuto
(Render free duerme el servicio); la app espera 60 s antes de fallar.

Smoke test: [`docs/architecture/e2e-smoke.md`](docs/architecture/e2e-smoke.md).

## Tests backend

```bash
cd backend && npm run test && npm run test:e2e
```

Con Postgres + API en marcha: `npm run test:e2e:live` y `npm run test:e2e:crud`.

Colección Postman: [`backend/collections/`](backend/collections/).
