# App Android — entrega v1

Cliente **offline-first** para ganaderos: Compose + Material 3, Room (caché + outbox), Retrofit, `SyncWorker` / sync manual desde **Más**.

## Conexión con la API

| Entorno | Base URL |
|---------|----------|
| Emulador Android | `http://10.0.2.2:3000/api/v1/` |
| Dispositivo físico (misma Wi‑Fi) | IP del PC, p. ej. `http://192.168.x.x:3000/api/v1/` |

Requisito: backend en `npm run start:dev` y Postgres (Docker).

## Flujo principal

1. **Splash / bienvenida** → **Login** o **Registro** (`RegisterRequest`: email, password, nombre, nombre finca).
2. Tras login, **MainActivity** con barra inferior (5 tabs): Inicio, Animales, Producción, Potreros, Más.
3. Stack secundario: Pendientes, Perfil, Finanzas, Reportes, Reproducción, formularios CRUD, detalle animal/potrero/reproducción.
4. **Sync**: automático al reanudar + botón en **Más**; outbox envía cambios con `POST /sync/pull` + `POST /sync/push`.
5. **Cambio de cuenta**: al cerrar sesión / otro login, se limpia Room (`LocalTenantClearer`) para no mezclar fincas.

## Funcionalidades implementadas

| Área | Comportamiento |
|------|----------------|
| **Inicio** | KPIs (local primero, luego `GET /dashboard/summary`), pendientes con check, campana → Pendientes, icono perfil → Perfil, tarjeta Reportes |
| **Animales** | Lista, alta, edición, detalle con tabs (incl. producción del animal) |
| **Potreros** | Lista, detalle, alta/edición, trabajos → tareas pendientes |
| **Producción** | Registro con unidad nativa, historial |
| **Reproducción** | Lista, registro, detalle de evento |
| **Pendientes** | Lista filtrada; marcar **completada** (sync `PENDING_TASK` UPDATE o API) |
| **Reportes** | Semanal / mensual con sesión |
| **Perfil** | Solo lectura vía `GET /auth/me` (nombre, email, finca) |
| **Finanzas** | Productividad por rubro (UI) |

## Usuarios de prueba

- **Demo:** `demo@agrocontrol.local` / `demo1234` — datos semilla tras sync.
- **Usuario nuevo:** registro → finca vacía en servidor; app sin datos ajenos tras limpieza local.

## Checklist manual (smoke)

Ver [`docs/architecture/e2e-smoke.md`](../architecture/e2e-smoke.md): login demo, perfil, completar tarea, usuario nuevo vacío, offline → sync.

## Código de referencia

| Tema | Ubicación |
|------|-----------|
| Navegación | `app/.../ui/navigation/AgroNavHost.kt`, `AgroDestinations.kt` |
| Inicio header | `app/.../ui/feature/inicio/InicioScreen.kt` |
| Perfil | `app/.../ui/feature/perfil/PerfilFeature.kt` |
| Sync | `app/.../sync/` |
| API | `app/.../network/ApiService.kt` |

## Diseño UI

Especificación Compose: [`docs/design/00-README.md`](../design/00-README.md). Pantallas clave: [`02-inicio.md`](../design/screens/02-inicio.md), [`21-perfil.md`](../design/screens/21-perfil.md).

## Documentación relacionada

- Entrega general: [`docs/ENTREGA.md`](../ENTREGA.md)
- API: [`docs/entrega/BACKEND-API.md`](BACKEND-API.md)
- Arquitectura: [`docs/architecture/overview.md`](../architecture/overview.md)
