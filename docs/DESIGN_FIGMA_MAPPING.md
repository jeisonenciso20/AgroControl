# Mapeo AgroGest (Figma) ↔ AgroControl (Android Compose)

**Figma:** [AgroGest](https://www.figma.com/design/WeMBtK1xWP0qNTtwllbwcL/AgroGest?node-id=0-1)

UI implementada en **Jetpack Compose** (Material 3, sistema «Campo» en `docs/design/01-design-system.md`).

## Design tokens

Centralizados en [`app/src/main/java/com/jeison/agrocontrol/ui/theme/Color.kt`](../app/src/main/java/com/jeison/agrocontrol/ui/theme/Color.kt) (primary `#2E6B30`, fondos cálidos, tarjetas con borde).

## Pantallas

| Frame Figma | Ruta Compose | Paquete | Estado |
|-------------|--------------|---------|--------|
| Splash / COMENZAR | Splash sistema + `bienvenida` | `ui/feature/auth` | OK |
| Iniciar sesión | `login` | `ui/feature/auth` | OK |
| Crear cuenta | `registro` | `ui/feature/auth` | OK |
| Dashboard | `inicio` | `ui/feature/inicio` | KPI + pendientes + sync |
| Animales — lista | `animales` | `ui/feature/animales` | OK |
| Animales — alta / edición | `animales/nuevo`, `animales/{codigo}/editar` | `ui/feature/animales` | OK |
| Animales — detalle | `animales/detalle/{codigo}` | `ui/feature/animales` | Tabs info; prod/repro placeholder |
| Producción | `produccion` | `ui/feature/produccion` | Datos locales |
| Potreros | `potreros`, `potreros/{serverId}` | `ui/feature/potreros` | OK |
| Reproducción | `reproduccion` | `ui/feature/reproduccion` | Placeholder (FeatureFlags) |
| Finanzas | `finanzas` | `ui/feature/finanzas` | Rubros locales |
| Reportes | `reportes` | `ui/feature/reportes` | API weekly/monthly |
| Pendientes | `pendientes` | `ui/feature/pendientes` | OK |
| Más | `mas` | `ui/feature/mas` | Sync, tema, logout |

## Componentes

| Componente | Archivo |
|------------|---------|
| Scaffold, KPI, listas, banner sync | [`ui/components/AgroComponents.kt`](../app/src/main/java/com/jeison/agrocontrol/ui/components/AgroComponents.kt) |
| Barra inferior 5 ítems | [`ui/navigation/AgroNavHost.kt`](../app/src/main/java/com/jeison/agrocontrol/ui/navigation/AgroNavHost.kt) |
| Iconos | [`ui/theme/AgroIcons.kt`](../app/src/main/java/com/jeison/agrocontrol/ui/theme/AgroIcons.kt) |

## Activity

Una sola: [`MainActivity.kt`](../app/src/main/java/com/jeison/agrocontrol/MainActivity.kt) con `AgroNavHost`.
