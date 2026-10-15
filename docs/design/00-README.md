# AgroControl - Especificación de diseño frontend (Jetpack Compose + Material 3)

> Carpeta: `docs/design/`. Esta especificación reemplaza la UI actual (XML + Activities) por una UI nueva en Jetpack Compose. Está escrita como instrucciones directas para agentes de Cursor. **No cambia la lógica de negocio**: Room, `SyncManager`, `SyncScheduler`, `SyncWorker`, `TokenStore`, `RetrofitClient`, `ApiService` y `AnimalRepository` se reutilizan tal cual.

## 1. Contexto de usuario (leer antes de diseñar o codificar)

Usuarios: ganaderos y trabajadores de finca en Colombia.

- Uso en campo, a pleno sol, a veces con guantes, manos húmedas o sucias.
- Conectividad baja o nula durante horas. La app debe sentirse completa sin red.
- Alfabetización digital variable: textos cortos, verbos claros ("Guardar animal", no "Submit"), iconos siempre acompañados de texto.
- Dispositivos Android de gama baja/media (minSdk 24), pantallas de 5.5 a 6.7 pulgadas.

Principios que gobiernan todas las decisiones:

1. **Legible al sol**: contraste mínimo 7:1 en texto principal (WCAG AAA), nunca texto gris claro sobre blanco.
2. **Grande y tocable**: objetivo táctil mínimo 56 dp en acciones primarias, 48 dp en todo lo demás.
3. **Offline primero, y que se note**: el usuario siempre sabe si lo que guardó está en el teléfono o ya en el servidor.
4. **Una tarea por pantalla**: formularios cortos, una acción principal visible.
5. **Sin emojis**. Toda iconografía sale de Material Symbols (`material-icons-extended`, estilo Rounded) más un icono propio de bovino. Ver `01-design-system.md`.

## 2. Índice

| Archivo | Contenido |
|---|---|
| [01-design-system.md](01-design-system.md) | Marca, color (claro/oscuro), tipografía, espaciado, formas, elevación, iconografía, movimiento, accesibilidad |
| [02-compose-migration.md](02-compose-migration.md) | Dependencias Gradle, arquitectura single-Activity, tema, paquetes, ViewModel/UiState, migración paso a paso, eliminación de XML |
| [03-components.md](03-components.md) | Catálogo de componentes reutilizables con parámetros y estados |
| [04-navigation.md](04-navigation.md) | Arquitectura de información, barra inferior, rutas, flujos |
| [screens/01-login.md](screens/01-login.md) | Inicio de sesión y registro |
| [screens/02-inicio.md](screens/02-inicio.md) | Inicio / Dashboard |
| [screens/03-animales-lista.md](screens/03-animales-lista.md) | Lista de animales |
| [screens/04-animal-detalle.md](screens/04-animal-detalle.md) | Detalle de animal |
| [screens/05-animal-formulario.md](screens/05-animal-formulario.md) | Agregar / editar animal |
| [screens/06-potreros.md](screens/06-potreros.md) | Potreros |
| [screens/07-produccion.md](screens/07-produccion.md) | Producción |
| [screens/08-reproduccion.md](screens/08-reproduccion.md) | Reproducción |
| [screens/09-finanzas.md](screens/09-finanzas.md) | Finanzas (productividad por rubro) |
| [screens/10-pendientes.md](screens/10-pendientes.md) | Pendientes (tareas) |
| [screens/11-reportes.md](screens/11-reportes.md) | Reportes semanal / mensual |
| [screens/12-mas-ajustes.md](screens/12-mas-ajustes.md) | Más: perfil, sincronización, ajustes, cerrar sesión |
| [screens/13-splash.md](screens/13-splash.md) | Splash y bienvenida |
| [screens/14-registro.md](screens/14-registro.md) | Crear cuenta |
| [screens/15-potrero-detalle.md](screens/15-potrero-detalle.md) | Detalle de potrero |
| [screens/16-potrero-formulario.md](screens/16-potrero-formulario.md) | Agregar / editar potrero |
| [screens/17-produccion-historial-animal.md](screens/17-produccion-historial-animal.md) | Historial de producción por animal |
| [screens/18-produccion-registrar.md](screens/18-produccion-registrar.md) | Registrar producción de leche |
| [screens/19-reproduccion-registrar.md](screens/19-reproduccion-registrar.md) | Registrar evento reproductivo |
| [screens/20-potrero-trabajo-registrar.md](screens/20-potrero-trabajo-registrar.md) | Registrar trabajo de potrero |
| [screens/21-perfil.md](screens/21-perfil.md) | Perfil (lectura, `auth/me`) |

**Entrega del proyecto (estado actual app + API):** [`docs/ENTREGA.md`](../ENTREGA.md) y [`docs/entrega/ANDROID-APP.md`](../entrega/ANDROID-APP.md).

## 3. Cómo usar estos documentos con Cursor

1. Abre el proyecto `D:\Proyectos\AgroControl\AgroControl` en Cursor.
2. Para cada paso del orden de implementación (sección 4) abre un chat de Agente nuevo y adjunta con `@` los archivos indicados. Ejemplo de prompt:

   ```text
   Implementa el paso 3 de @docs/design/00-README.md.
   Sigue exactamente @docs/design/01-design-system.md y @docs/design/02-compose-migration.md.
   No modifiques la lógica de negocio (data/, sync/, network/, auth/) salvo lo que el documento marque como "Adición permitida".
   Al terminar compila con ./gradlew :app:assembleDebug y corrige errores.
   ```

3. Para cada pantalla: adjunta `@docs/design/03-components.md`, `@docs/design/04-navigation.md` y el archivo de la pantalla en `screens/`. Pide al agente que verifique cada **criterio de aceptación** de la pantalla y marque los que cumple.
4. Un paso = un commit. Mensaje sugerido: `feat(ui): <paso>`.
5. Si un agente propone usar emojis, colores hex sueltos o `dp` mágicos fuera de los tokens, rechaza el cambio: todo debe salir de `AgroTheme`, `AgroSpacing` y `AgroIcons`.

### Reglas globales para cualquier agente

- Idioma de la UI: español de Colombia. Fechas `dd/MM/yyyy` en pantalla, `yyyy-MM-dd` en datos. Números con separador de miles `.` y decimal `,` (`Locale("es", "CO")`).
- Prohibido: emojis, `Toast` (usar `Snackbar`), colores hex fuera de `ui/theme/Color.kt`, textos hardcodeados fuera de `strings.xml` (se acepta en una primera pasada, pero el paso 13 los extrae).
- Cada pantalla tiene: `XxxScreen` (stateless, recibe `UiState` + lambdas), `XxxRoute` (conecta ViewModel), `XxxViewModel`, `XxxUiState`, y al menos un `@Preview` claro y uno oscuro.
- No tocar `backend/`.

## 4. Orden de implementación

| Paso | Qué | Documentos |
|---|---|---|
| 1 | Dependencias Compose + plugin + `buildFeatures.compose = true` (conviven XML y Compose temporalmente) | 02 §2 |
| 2 | Tema: `Color.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `Theme.kt`, `AgroIcons.kt`, fuente | 01, 02 §4 |
| 3 | Infraestructura: `AppContainer`, `ConnectivityObserver`, `SyncStatusRepository` (solo lectura), `Formatters` | 02 §5-6 |
| 4 | Componentes base del catálogo + previews | 03 |
| 5 | `MainActivity` single-Activity + `AgroNavHost` + barra inferior + pantallas placeholder | 04, 02 §7 |
| 6 | Login | screens/01 |
| 7 | Inicio | screens/02 |
| 8 | Animales: lista, detalle, formulario (alta) | screens/03, 04, 05 |
| 9 | Pendientes y Producción | screens/10, 07 |
| 10 | Potreros, Finanzas, Reportes, Reproducción | screens/06, 09, 11, 08 |
| 11 | Más / Ajustes (sync manual, cerrar sesión, tema) + Splash/Bienvenida + Registro | screens/12, 13, 14 |
| 11b | Detalle de potrero (lectura) | screens/15 |
| 11c | Agregar componente `BarChart` (Vico) e Historial por animal con datos disponibles | 03 §10, screens/17 |
| 11d | Formularios con dependencia backend (potrero, producción, reproducción, trabajo) tras `FeatureFlags`; activar cuando backend/persistencia estén listos | screens/16, 18, 19, 20 |
| 12 | Edición de animal (requiere adición permitida en `AnimalRepository`) | screens/05 §Edición |
| 13 | Eliminar Activities y XML antiguos, `viewBinding = false`, extraer strings, pruebas de UI | 02 §9 |

## 5. Hallazgos del análisis del código actual (motivan el rediseño)

- Emojis como iconos (`Animal.tituloDisplay()` y textos del dashboard muestran `??` por problemas de codificación). En la UI nueva **no se usa `tituloDisplay()` ni `detalleLineas()`**; se construye el título en la capa UI.
- Módulos (Potreros, Producción, Pendientes, Finanzas, Reportes) muestran un único `TextView` con texto concatenado: se reemplaza por listas reales con tarjetas.
- Mensajes con `Toast` y tildes corruptas (`C�digo`): todos los archivos nuevos en UTF-8.
- Potreros del formulario de animal están hardcodeados ("Potrero 1..3"): la UI nueva los lee de `LocalCacheDao.allPotreros()` con opción de texto libre si la caché está vacía.
- La "edición" de animal y los accesos de Detalle a producción/reproducción eran `Toast` "Próximamente".
- No hay indicador de sincronización ni de conexión.
- El botón "Registrarse" crea una cuenta demo aleatoria: se reemplaza por un formulario real usando `RegisterRequest(email, password, nombre, fincaNombre)`.

## 6. Figma vs nueva propuesta

Referencia: [AgroGest en Figma](https://www.figma.com/design/WeMBtK1xWP0qNTtwllbwcL/AgroGest?node-id=0-1). Inventariado con sesión iniciada (15 frames). Regla: **se cubre cada pantalla, campo y flujo que el Figma pretende, pero con el sistema de diseño propio** (`01-design-system.md`). No se copian estilos, colores (`#F5E7E7` rosado de tarjetas), emojis ni tipografía del Figma.

### 6.1 Cobertura frame por frame

| # | Frame Figma | Documento | Datos |
|---|---|---|---|
| 1 | Splash (foto, logo, wordmark, COMENZAR) | [screens/13-splash.md](screens/13-splash.md) | Ninguno |
| 2 | Iniciar sesión | [screens/01-login.md](screens/01-login.md) | Existente + "Recordar usuario" (pref. local); "Olvidaste tu clave" = **dependencia backend** |
| 3 | Crear cuenta | [screens/14-registro.md](screens/14-registro.md) | Existente (`RegisterRequest`); confirmación de contraseña solo en cliente |
| 4 | Dashboard | [screens/02-inicio.md](screens/02-inicio.md) | Existente (`DashboardSummary`); campana sin API de notificaciones -> usa pendientes |
| 5 | Animales | [screens/03-animales-lista.md](screens/03-animales-lista.md) | Existente; foto = **dependencia backend/datos** |
| 6 | Potreros (lista) | [screens/06-potreros.md](screens/06-potreros.md) | Parcial; próximo trabajo derivable de `PendingTaskLocal` |
| 7 | Detalle potrero | [screens/15-potrero-detalle.md](screens/15-potrero-detalle.md) | Parcial; estado del pasto, última ocupación = **dependencia backend** |
| 8 | Detalle animal (tabs) | [screens/04-animal-detalle.md](screens/04-animal-detalle.md) | Existente + tabs que dependen de producción por animal y reproducción |
| 9 | Agregar animal | [screens/05-animal-formulario.md](screens/05-animal-formulario.md) | Existente; foto = dependencia |
| 10 | Agregar potrero | [screens/16-potrero-formulario.md](screens/16-potrero-formulario.md) | **Dependencia backend** (sin outbox de potreros) |
| 11 | Historial producción por animal | [screens/17-produccion-historial-animal.md](screens/17-produccion-historial-animal.md) | **Dependencia** (`ProductionLocal` sin `animalId`) |
| 12 | Reproducción | [screens/08-reproduccion.md](screens/08-reproduccion.md) | **Dependencia backend** (sin endpoint/caché en app) |
| 13 | Producción de leche | [screens/07-produccion.md](screens/07-produccion.md) | Parcial; por vaca y jornada = dependencia |
| 14 | Registrar producción (mal titulado "reproducción" en Figma) | [screens/18-produccion-registrar.md](screens/18-produccion-registrar.md) | **Dependencia** (outbox `PRODUCTION`, `animalId`, `jornada`) |
| - | Registrar evento reproductivo (no existe en Figma; diseño propio) | [screens/19-reproduccion-registrar.md](screens/19-reproduccion-registrar.md) | **Dependencia backend** |
| 15 | Registrar trabajo de potrero | [screens/20-potrero-trabajo-registrar.md](screens/20-potrero-trabajo-registrar.md) | Se modela como `PendingTask`; crear = **dependencia** (outbox `TASK`) |

Convención en todas las fichas: cada campo se marca **[Existe]** (modelo/API actual), **[UI]** (solo estado de interfaz o preferencia local) o **[Backend]** (requiere campo/endpoint/outbox nuevo; coordinar con `docs/agents/backend` y `persistence`). Las pantallas o controles **[Backend]** se construyen completos en UI pero se ocultan tras `FeatureFlags` hasta que exista el soporte. Ver resumen en §6.3.

### 6.2 Problemas del Figma que NO se replican

| Figma | Nueva propuesta |
|---|---|
| Erratas y textos sin tildes ("contrasena", "Ano", "Recuperacion") | Textos revisados en `strings.xml`, español correcto |
| "Vaca 001" vs "Vaca 01" | Código siempre tal como se guardó en `Animal.codigo`; título `nombre.ifBlank { "$tipo $codigo" }`; sin re-formatear ceros |
| Emojis como iconos | `AgroIcons` (Material Symbols Rounded) |
| Barra inferior de 5 en unas pantallas y 4 en otras, con orden distinto | Una sola barra de 5 destinos fija (ver `04-navigation.md` §2) |
| Tarjetas rosadas `#F5E7E7`, pasteles | Tarjetas `surfaceContainerLowest` + borde, acentos solo en iconos |
| "Crear cuenta" sin botón volver | TopAppBar con Volver + Atrás del sistema |
| "Registrar producción" titulado "Registrar reproducción" | Títulos correctos y formularios separados |
| Splash con botón COMENZAR obligatorio en cada arranque | Bienvenida solo la primera vez; arranques posteriores usan SplashScreen del sistema |
| Botones en MAYÚSCULAS (INGRESAR, REGISTRARME) | Mayúscula inicial ("Ingresar"), más legible |

### 6.3 Dependencias de backend/datos generadas por el Figma

1. Foto de animal (`fotoUri` local + subida) - opcional, v2.
2. Recuperar contraseña (`POST /auth/forgot-password`).
3. Crear/editar potrero vía sync (`entity = "POTRERO"`) + campos `tipoPasto`, `estadoPasto`, `fechaUltimaSiembra`, `fechaProximoUso`, `observacion`, `ultimaOcupacion`.
4. Producción: crear vía sync (`entity = "PRODUCTION"`) + campos `animalCodigo/animalId`, `jornada` (MANANA/TARDE).
5. Reproducción: endpoint de lectura en `ApiService`, entidad Room `reproduction_events`, sync `entity = "REPRODUCTION"`, estado reproductivo por animal.
6. Trabajos de potrero: crear `PendingTask` vía sync (`entity = "TASK"`) con `referencia` = potrero.
7. Notificaciones (campana): sin API; v1 usa conteo de pendientes.

Cada dependencia se implementa en `data/`/`sync/` por el agente de persistencia **antes** de activar su flag; la UI no inventa datos.
