# 04 - Navegación y arquitectura de información

## 1. Arquitectura de información (reconciliada con Figma)

```text
ARRANQUE
└─ Splash del sistema -> (primera vez) Bienvenida "Comenzar" -> Login

AUTH (sin barra inferior)
├─ Login  ─(Crear cuenta)─> Registro  (con Volver)
└─ Login  ─(¿Olvidaste tu contraseña?)─> diálogo informativo / flujo backend

MAIN (barra inferior + SyncStatusBanner)
├─ Inicio        [tab 1]  header: campana (pendientes) + perfil (-> Más)
│   ├─> Pendientes (Ver todos / campana)
│   ├─> Reportes (tarjeta "Ver reportes")
│   └─> KPI: Animales, Producción, Potreros, Reproducción
├─ Animales      [tab 2]
│   ├─ Detalle de animal [tabs Información | Producción | Reproducción]
│   │   ├─ Editar animal
│   │   ├─ Historial de producción del animal
│   │   └─ Registrar evento reproductivo (preseleccionado)
│   └─ Agregar animal
├─ Producción    [tab 3]  (Producción de leche)
│   ├─ Registrar producción
│   └─ Historial por animal
├─ Potreros      [tab 4]
│   ├─ Detalle de potrero
│   │   ├─ Registrar trabajo (preseleccionado)
│   │   └─ Editar potrero
│   └─ Agregar potrero
└─ Más           [tab 5]
    ├─ Reproducción ─> Registrar evento reproductivo
    ├─ Finanzas (productividad)
    ├─ Reportes
    ├─ Pendientes
    ├─ Sincronización, Apariencia, Versión
    └─ Cerrar sesión
```

## 2. Barra inferior (decisión única)

| Orden | Etiqueta | Icono | Ruta | Badge |
|---|---|---|---|---|
| 1 | Inicio | `AgroIcons.Inicio` | `inicio` | - |
| 2 | Animales | `AgroIcons.Bovino` | `animales` | - |
| 3 | Producción | `AgroIcons.Produccion` | `produccion` | - |
| 4 | Potreros | `AgroIcons.Potrero` | `potreros` | - |
| 5 | Más | `AgroIcons.Mas` | `mas` | punto si hay cambios sin enviar |

Decisión y justificación (reemplaza la versión anterior de este documento y las dos barras inconsistentes del Figma):
- El Figma usa a veces 5 tabs (Inicio, Finanzas, Animales, Potreros, Más) y a veces 4. Se fija **una** barra de 5 destinos en todas las pantallas MAIN.
- **Potreros entra a la barra** (está en el Figma y tiene ahora lista, detalle, alta y registro de trabajos: es un módulo de uso diario en campo).
- **Producción entra a la barra** en lugar de Finanzas: el ordeño se registra dos veces al día (mañana/tarde, frame 13-14); es la acción más frecuente del trabajador.
- **Finanzas sale a Más**: en este producto es "productividad por rubro" (consulta del administrador, semanal/mensual) y no tiene captura de datos.
- **Pendientes deja de ser tab**: se accede desde la lista "Pendientes" de Inicio, "Ver todos", la campana del header (con badge = vencidas + hoy) y desde Más. Así se respeta el header del Figma (campana) sin duplicar destinos.
- **Reproducción** se accede por su KPI en Inicio, el tab Reproducción del Detalle de animal y Más.
- Orden: de lo general (Inicio) a los recursos (Animales, Producción, Potreros) y lo secundario al final (Más), igual que la intención del Figma.

Comportamiento:
- `alwaysShowLabel = true`. Al tocar el tab activo: volver a la raíz y hacer scroll al inicio.
- Navegación de tab con `popUpTo(graph.findStartDestination().id) { saveState = true }`, `launchSingleTop = true`, `restoreState = true`.
- La barra se oculta en: Bienvenida, Login, Registro y **todos los formularios** (Agregar/Editar animal, Agregar potrero, Registrar producción, Registrar evento reproductivo, Registrar trabajo). Se mantiene en detalles y módulos secundarios.
- Atrás del sistema: en un tab distinto de Inicio vuelve a Inicio; en Inicio sale de la app.

## 3. Rutas (`AgroDestinations.kt`)

```kotlin
object Graph { const val AUTH = "auth"; const val MAIN = "main" }

sealed class Dest(val route: String) {
    data object Bienvenida : Dest("bienvenida")
    data object Login : Dest("login")
    data object Registro : Dest("registro")
    data object Inicio : Dest("inicio")
    data object Animales : Dest("animales")
    data object AnimalNuevo : Dest("animales/nuevo")
    data object AnimalDetalle : Dest("animales/{codigo}?tab={tab}") {   // tab: info | produccion | reproduccion
        fun create(codigo: String, tab: String = "info") = "animales/${Uri.encode(codigo)}?tab=$tab"
    }
    data object AnimalEditar : Dest("animales/{codigo}/editar") { fun create(c: String) = "animales/${Uri.encode(c)}/editar" }
    data object AnimalProduccion : Dest("animales/{codigo}/produccion") { fun create(c: String) = "animales/${Uri.encode(c)}/produccion" }
    data object Produccion : Dest("produccion")
    data object ProduccionRegistrar : Dest("produccion/registrar?codigo={codigo}") {
        fun create(codigo: String? = null) = "produccion/registrar" + (codigo?.let { "?codigo=${Uri.encode(it)}" } ?: "")
    }
    data object Potreros : Dest("potreros")
    data object PotreroNuevo : Dest("potreros/nuevo")
    data object PotreroDetalle : Dest("potreros/{serverId}") { fun create(id: String) = "potreros/${Uri.encode(id)}" }
    data object PotreroEditar : Dest("potreros/{serverId}/editar") { fun create(id: String) = "potreros/${Uri.encode(id)}/editar" }
    data object TrabajoRegistrar : Dest("potreros/trabajo?potrero={potrero}") {
        fun create(potrero: String? = null) = "potreros/trabajo" + (potrero?.let { "?potrero=${Uri.encode(it)}" } ?: "")
    }
    data object Reproduccion : Dest("reproduccion")
    data object ReproduccionRegistrar : Dest("reproduccion/registrar?codigo={codigo}") {
        fun create(codigo: String? = null) = "reproduccion/registrar" + (codigo?.let { "?codigo=${Uri.encode(it)}" } ?: "")
    }
    data object Pendientes : Dest("pendientes")
    data object Finanzas : Dest("finanzas")
    data object Reportes : Dest("reportes")
    data object Mas : Dest("mas")
}
```

Notas:
- Declarar rutas literales (`animales/nuevo`, `potreros/nuevo`, `potreros/trabajo`) **antes** de las que tienen argumento.
- Animal se identifica por `codigo` (único en Room). Potrero por `PotreroLocal.serverId` (clave primaria actual); el vínculo animal-potrero es por **nombre** (`Animal.potrero` = `PotreroLocal.nombre`), igual que hoy.
- Rutas de formularios con dependencia backend se registran siempre, pero sus puntos de entrada (FAB, botones) se ocultan con `FeatureFlags` hasta tener soporte.

## 4. Flujos

### 4.1 Arranque y sesión
```text
App abre -> Splash del sistema (installSplashScreen, logo sobre primary)
  primera ejecución (pref "onboarding_visto" = false)? -> Bienvenida -> "Comenzar" -> Login

  tokenStore.isLoggedIn()?
    sí -> Graph.MAIN / inicio  (dispara syncAll en segundo plano)
    no -> Graph.AUTH / login
Login OK -> saveTokens -> syncAll() (no bloquear más de 5 s; continuar aunque falle)
        -> navigate(Graph.MAIN) { popUpTo(Graph.AUTH) { inclusive = true } }
Cerrar sesión (Más) -> ConfirmDialog -> tokenStore.clear()
        -> navigate(Graph.AUTH) { popUpTo(Graph.MAIN) { inclusive = true } }
```
Si una llamada de API devuelve 401 (token vencido; hoy no hay refresh en la app): mostrar Snackbar "Tu sesión expiró. Vuelve a ingresar." con acción "Ingresar" que ejecuta el flujo de cerrar sesión **sin borrar datos locales** (Room). No implementar refresh en esta fase (es lógica de auth).

### 4.2 Registrar animal (offline)
```text
Animales -> FAB "Agregar animal" -> Formulario
  Guardar -> validar -> AnimalRepository.insert (Room + outbox + schedule)
    OK -> popBackStack() -> Snackbar "Animal guardado..." -> lista recarga (ON_RESUME)
    código duplicado -> error en campo Código "Ya existe un animal con este código"
  Volver con cambios -> ConfirmDialog "¿Descartar cambios?"
```

### 4.3 Consultar animal
```text
Animales -> buscar -> tocar ítem -> Detalle(codigo)
Detalle -> "Editar" -> AnimalEditar(codigo) -> Guardar -> popBackStack a Detalle (recarga)
```

### 4.4 Sincronización manual
```text
Banner "N cambios por enviar" -> "Enviar ahora"  ─┐
Más -> Sincronización -> "Sincronizar ahora"      ─┴-> SyncStatusRepository.syncNow()
   -> banner "Enviando cambios..." -> éxito: Snackbar "Todo enviado" / error: banner de error con "Reintentar"
```

### 4.5 Registrar producción (ordeño)
```text
Producción -> "Registrar producción" (o tarjeta de vaca -> registrar con animal preseleccionado)
  -> Formulario (animal, fecha=hoy, jornada auto mañana/tarde por hora, litros, observación)
  -> Guardar -> Room + outbox PRODUCTION -> volver -> Snackbar -> totales del día recalculados
  "Guardar y registrar otra" mantiene fecha y jornada, limpia animal y litros (ordeño en serie)
```

### 4.6 Potreros y trabajos
```text
Potreros -> tarjeta -> Detalle potrero -> "Registrar trabajo" -> Formulario (potrero preseleccionado)
  -> Guardar -> PendingTask (outbox TASK) -> aparece en "Trabajos pendientes" del potrero, en Pendientes e Inicio
Potreros -> "Agregar potrero" -> Formulario -> Guardar -> lista
```

### 4.7 Reproducción
```text
Reproducción (o Detalle animal > tab Reproducción) -> "Registrar evento"
  -> Formulario (hembra, tipo de evento, fecha, campos según tipo) -> Guardar -> estado reproductivo actualizado
  Parto con cría -> ofrecer "Registrar la cría" -> Agregar animal (tipo Ternero/Ternera, fecha = fecha del parto)
```

### 4.8 Deep links
No requeridos en esta fase.
