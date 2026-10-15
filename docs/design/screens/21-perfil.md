# Pantalla: Perfil (solo lectura)

Ruta: `perfil` (stack desde Inicio). Paquete `ui/feature/perfil`.

## Propósito

Mostrar datos de la cuenta y la finca activa sin edición (v1). Fuente: **`GET /auth/me`** con JWT en sesión.

## Wireframe

```text
┌────────────────────────────────────┐
│ ← Perfil                           │  TopAppBar
├────────────────────────────────────┤
│ (Usuario)                          │
│ Nombre completo                    │  titleMedium
│ email@ejemplo.com                  │  bodyMedium onSurfaceVariant
│                                    │
│ Finca                              │  label
│ Mi finca                           │  bodyLarge
│                                    │
│ (Sin botones Guardar en v1)        │
└────────────────────────────────────┘
```

## Datos

| Campo UI | API / modelo |
|----------|----------------|
| Nombre | `MeResponse.nombre` (o equivalente en DTO) |
| Email | `MeResponse.email` |
| Finca | `MeResponse.fincaNombre` / finca asociada al token |

Estados: loading (scaffold), error de red → mensaje + reintentar opcional; offline sin caché de perfil → mostrar lo guardado en login si existe, si no aviso.

## Navegación

- Entrada: icono **Perfil** en `InicioHomeHeader` (`AgroIcons.Usuario`).
- Salida: `onBack` → pop stack a Inicio.

## Criterios de aceptación

- [ ] Solo lectura; no formulario de edición en v1.
- [ ] Llama a `auth/me` con token válido tras abrir pantalla.
- [ ] TopAppBar con título "Perfil" y navegación atrás.
- [ ] Textos en español, sin emojis.

## Implementación

`PerfilRoute`, `PerfilViewModel`, `PerfilUiState` en `PerfilFeature.kt`. Registrado en `AgroNavHost` como `Dest.Perfil`.
