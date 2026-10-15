# Pantalla: Inicio (Dashboard)

Ruta: `inicio` (tab 1). Reemplaza `MainActivity` (dashboard) + `activity_main.xml`. Paquete `ui/feature/inicio`.

## Propósito
En 3 segundos el usuario sabe: cómo va la finca, qué tiene pendiente hoy, y si sus datos están enviados. Puerta a los módulos secundarios.

## Encabezado (implementado)

- Fila superior con **`statusBarsPadding()`** para respetar barra de estado (safe area).
- Saludo + fecha a la izquierda; a la derecha: **campana** (→ Pendientes, badge opcional) e icono **Perfil** (→ `Dest.Perfil`, pantalla [`21-perfil.md`](21-perfil.md)).
- Componente: `InicioHomeHeader` en `InicioScreen.kt`.

## Wireframe
```text
┌────────────────────────────────────┐
│ Buenos días         (Campana)(Perfil)│  statusBarsPadding; Perfil → perfil
│ Miércoles 14 de octubre            │  bodyLarge onSurfaceVariant
├────────────────────────────────────┤
│ [SyncStatusBanner si aplica]       │
├────────────────────────────────────┤
│ Pendientes de hoy        Ver todos │  SectionHeader
│ ┌────────────────────────────────┐ │
│ │ (Vencido) Vacunar lote 2       │ │  TaskListItem x máx 3
│ │           Venció el 12/10      │ │
│ │ (Reloj)   Pesaje terneros      │ │
│ │           Vence hoy            │ │
│ └────────────────────────────────┘ │
│                                    │
│ Resumen                            │
│ ┌──────────────┐ ┌──────────────┐  │
│ │ (Bovino)     │ │ (Leche)      │  │  KpiTile grid 2 columnas
│ │ 124          │ │ 1.540 litros │  │
│ │ Animales     │ │ Leche semana │  │
│ └──────────────┘ └──────────────┘  │
│ ┌──────────────┐ ┌──────────────┐  │
│ │ (Potrero)    │ │ (Reprod.)    │  │
│ │ 8            │ │ 5            │  │
│ │ Potreros     │ │ Eventos rep. │  │
│ └──────────────┘ └──────────────┘  │
│                                    │
│ Accesos rápidos                    │
│ [Potreros][Reproducción][Finanzas] │  QuickActionButton grid 3 col
│ [Reportes]                         │
├────────────────────────────────────┤
│ Inicio Animales Producción Potr. Más│
└────────────────────────────────────┘
```
`LazyColumn` (o `LazyVerticalGrid` con `span` para encabezados). Sin FAB. Pull-to-refresh (`PullToRefreshBox`) dispara `syncNow()` y recarga; además existe acción visible en el banner/Más (no depender solo del gesto).

## Datos y origen (local primero, API cuando hay red)
| Elemento | Local (siempre) | API (si hay red y sesión) |
|---|---|---|
| Animales | `animalRepository.count()` | `DashboardSummary.animalesRegistrados` |
| Leche semana | Suma de `LocalCacheDao.allProduction()` con `productType` que contenga "leche" (ignore case) y `fecha` en últimos 7 días | `DashboardSummary.litrosLecheSemana` |
| Potreros | `LocalCacheDao.allPotreros().size` | `DashboardSummary.potrerosRegistrados` |
| Eventos reproducción | No hay dato local -> `KpiState.Unavailable` | `DashboardSummary.eventosReproduccion` |
| Pendientes | `LocalCacheDao.pendingTasks()` (ordenadas por `vencimiento`), top 3 | `DashboardSummary.pendientesRecientes` (`PendingTaskDto: id, titulo, vencimiento, tipo`) |

Regla: pintar primero los valores locales; al llegar el summary, reemplazar. Si la API falla, mantener locales sin mostrar error de pantalla completa (solo `LastSyncText`). Al reanudar (`ON_RESUME`) ejecutar `syncManager.syncAll()` como hoy, en el ViewModel.

```kotlin
data class InicioUiState(
    val saludo: UiText, val fechaHoy: String,
    val animales: KpiValue, val lecheSemana: KpiValue, val potreros: KpiValue, val reproduccion: KpiValue,
    val pendientes: List<TaskItemUi>, val pendientesTotal: Int,
    val isRefreshing: Boolean = false, val lastPullIso: String? = null,
)
sealed interface KpiValue { data object Loading : KpiValue; data object Unavailable : KpiValue; data class Value(val text: String) : KpiValue }
```

## Interacciones
- KPI Animales -> tab Animales. KPI Leche -> tab Producción. KPI Potreros -> Potreros. KPI Reproducción -> Reproducción.
- Tarea -> tab Pendientes. "Ver todos" -> Pendientes.
- Accesos rápidos -> Potreros, Reproducción, Finanzas, Reportes.

## Estados
- Sin pendientes: tarjeta compacta con icono `Exito` y "No tienes tareas pendientes".
- Sin caché y sin red (primer uso offline): KPI locales en 0/"-", banner offline; no pantalla de error.

## Criterios de aceptación
- [ ] Muestra datos locales en < 500 ms sin red.
- [ ] Ningún texto con emoji ni `??`; cifras con separador de miles.
- [ ] Cada KPI y acceso rápido tiene alto >= 96 dp y navega correctamente.
- [ ] Tareas vencidas en rojo con icono y texto "Venció el ...".
- [ ] Legible con brillo alto al sol (verificación manual en exterior).

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

Frame 4 "Dashboard".

```text
┌────────────────────────────────────┐
│ Hola, Ganadero      (Campana)(Perfil)│  headlineMedium; IconButtons 48dp; badge en campana
│ Este es el resumen de tu finca     │  bodyLarge
│ [SyncStatusBanner]                 │
│ ┌─────────────┐ ┌─────────────┐    │  KpiTile 2x2 (orden Figma)
│ │ Animales 45 │ │ Producción  │    │
│ │             │ │ 120 litros  │    │  leche de HOY (ver abajo)
│ ├─────────────┤ ├─────────────┤    │
│ │ Potreros 8  │ │Reproducción │    │
│ │             │ │ 8 eventos   │    │
│ └─────────────┘ └─────────────┘    │
│ Pendientes                Ver todos│
│ (Potrero) Potrero 3 · Fumigación   │  TaskListItem x3
│           03/10                    │
│ (Reprod)  Vaca 024 · Inseminación  │
│           programada               │
│ ┌────────────────────────────────┐ │
│ │ (Reportes) Ver reportes      > │ │  AgroCard clicable (tertiaryContainer en el icono)
│ │ Resumen semanal y mensual      │ │
│ └────────────────────────────────┘ │
│ Inicio Animales Producción Potr. Más│
└────────────────────────────────────┘
```

| Elemento Figma | Decisión | Datos |
|---|---|---|
| "Hola, Ganadero" | "Hola, {nombre}" si hay nombre guardado; si no, saludo por hora "Buenos días" (sin inventar "Ganadero") | [UI] / perfil [Backend] |
| Campana | Abre Pendientes; badge = vencidas + hoy. No hay API de notificaciones | [Existe] `LocalCacheDao.pendingTasks()` |
| Perfil | Abre pantalla **Perfil** (`GET /auth/me`, solo lectura) | [Existe] ver [21-perfil.md](21-perfil.md) |
| KPI Producción "120 L" | Mostrar **litros de hoy** si hay registros de hoy, si no "litros esta semana" (`DashboardSummary.litrosLecheSemana`). Caption explícito para no confundir | [Existe] |
| KPI Reproducción "8" | `DashboardSummary.eventosReproduccion` | [Existe] (requiere red) |
| Pendientes con fecha 03/10 | `dd/MM` si es del año actual | [Existe] |
| Ver reportes | Tarjeta que abre Reportes | [Existe] |

La sección "Accesos rápidos" de la versión anterior se reduce a la tarjeta "Ver reportes" (Potreros ahora es tab; Finanzas y Reproducción en Más y KPI).
