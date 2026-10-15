# Pantalla: Splash y Bienvenida

Rutas: Splash del sistema (no es ruta) y `bienvenida` (grafo `auth`, primera ejecución). Figma frame 1. Paquete `ui/feature/bienvenida`.

Leyenda: [Existe] · [UI] · [Backend] (ver 00-README §6.1).

## Propósito
Arranque rápido con identidad de marca, y una sola pantalla de bienvenida la primera vez. El Figma muestra foto de ganado, logo de vaca, wordmark y botón COMENZAR; aquí ese botón **no** bloquea cada arranque.

## 1. Splash del sistema (todas las veces)
- `installSplashScreen()` en `MainActivity`; tema `Theme.AgroControl.Splash`: fondo `#2E6B30` (primary), icono = isotipo blanco (vector adaptativo 108 dp, zona segura 72 dp).
- `setKeepOnScreenCondition { !ready }` solo mientras se lee `TokenStore.isLoggedIn()` y `onboarding_visto` (máx. ~300 ms). Sin animación larga ni temporizador artificial.

## 2. Bienvenida (solo primera ejecución)
```text
┌────────────────────────────────────┐
│                                    │
│   [ foto de ganado a sangre       ]│  imagen 100% ancho, alto 60%
│   [ con scrim inferior 45%        ]│
│                                    │
│   (isotipo 56dp blanco)            │
│   AgroControl                      │  headlineMedium Bold, blanco sobre scrim
│   Tu finca al día, aun sin señal   │  bodyLarge
├────────────────────────────────────┤  panel inferior surface, forma large arriba
│ (Bovino)  Registra tus animales    │  3 beneficios: icono 24dp + bodyLarge
│ (Leche)   Anota el ordeño diario   │
│ (SinInternet) Funciona sin internet│
│                                    │
│ [          Comenzar            ]   │  PrimaryButton 56dp
│     Ya tengo cuenta: Ingresar      │  TextButton 48dp
└────────────────────────────────────┘
```

## Datos
- `onboarding_visto` [UI] en `agrocontrol_ui_prefs`. "Comenzar" -> marca `true` -> `registro`. "Ingresar" -> marca `true` -> `login`.
- Si `TokenStore.isLoggedIn()` [Existe] -> ir directo a `Graph.MAIN` sin mostrar Bienvenida.

## Reglas de diseño
- Imagen según 01 §10. Texto sobre foto solo dentro del scrim, contraste >= 7:1.
- Sin emojis; sin "COMENZAR" en mayúsculas.
- Modo oscuro: mismo diseño (la foto ya es oscura con scrim); panel inferior `surface` oscuro.

## Criterios de aceptación
- [ ] Primera instalación: Splash -> Bienvenida -> Registro/Login.
- [ ] Siguientes aperturas: Splash -> Inicio (con sesión) o Login (sin sesión), sin Bienvenida.
- [ ] Splash no dura más de lo necesario para leer preferencias.
