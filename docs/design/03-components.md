# 03 - Catálogo de componentes

Ubicación: `ui/components/`. Cada componente: stateless, parámetro `modifier: Modifier = Modifier` como primer opcional, `@Preview` claro/oscuro y con `fontScale = 1.5f`. Tokens de `01-design-system.md`. Nombres con prefijo `Agro`.

## 1. Estructura de pantalla

### `AgroScaffold`
Envuelve `Scaffold` de M3 para pantallas internas.

| Parámetro | Tipo | Notas |
|---|---|---|
| title | String | `titleLarge`, `heading()` |
| onBack | (() -> Unit)? | Si no es null muestra botón Volver 48 dp con `AgroIcons.Volver`, cd "Volver" |
| actions | @Composable RowScope.() -> Unit | Máx. 2 iconos de acción |
| fab | @Composable () -> Unit | Opcional |
| isLoading | Boolean | `LinearProgressIndicator` de 4 dp bajo la barra |
| snackbarHostState | SnackbarHostState | |
| content | @Composable (PaddingValues) -> Unit | |

TopAppBar: `TopAppBar` (no centrada), fondo `background`, al hacer scroll `surfaceContainer` (`TopAppBarDefaults.pinnedScrollBehavior`). Las pantallas raíz de la barra inferior no llevan `onBack`.

### `SectionHeader(title, actionLabel?, onAction?)`
`titleMedium` + `heading()`; acción opcional como `TextButton` ("Ver todos"), alto 48 dp.

## 2. Estado de sincronización y conexión

### `SyncStatusBanner`
Banda fija bajo la TopAppBar (dentro de `AgroApp`, visible en todas las pantallas autenticadas). Alto mínimo 48 dp, ancho completo, icono 20 dp + texto `bodyMedium` + acción opcional.

| Estado (prioridad descendente) | Condición | Fondo / texto | Icono | Texto | Acción |
|---|---|---|---|---|---|
| Sin internet con pendientes | `!isOnline && pendingCount > 0` | `warningContainer` / `onWarningContainer` | `SinInternet` | "Sin internet. {n} cambios guardados en el teléfono" | - |
| Sin internet | `!isOnline` | `offlineContainer` / `offline` | `SinInternet` | "Sin internet. Puedes seguir trabajando" | - |
| Sincronizando | `isSyncing` | `tertiaryContainer` | `Sincronizar` (rotación lenta 1.2 s, desactivar si animaciones off) | "Enviando cambios..." | - |
| Error | `lastError` | `errorContainer` | `Error` | "No se pudo sincronizar" | "Reintentar" |
| Pendientes con red | `pendingCount > 0` | `warningContainer` | `PorEnviar` | "{n} cambios por enviar" | "Enviar ahora" |
| Todo al día | resto | **oculto** (no ocupar espacio) | - | - | - |

`semantics { liveRegion = LiveRegionMode.Polite }`. Animación 01 §8.

### `SyncStateChip(pendiente: Boolean)`
Etiqueta compacta por registro (alto 28 dp, no interactiva). Pendiente: `PorEnviar` + "Por enviar" en `warningContainer`. Sincronizado: `Sincronizado` + "Enviado" en `successContainer`. Mapea `Animal.syncState` (`PENDING`/`SYNCED`).

### `LastSyncText(lastPullIso)`
`bodySmall` `onSurfaceVariant`: "Actualizado {formatHaceCuanto}" o "Nunca sincronizado".

## 3. Tarjetas e indicadores

### `AgroCard`
`Card` con `surfaceContainerLowest`, borde 1 dp `outlineVariant`, forma `medium`, padding 16 dp. Variante clicable: `onClick` != null => alto mínimo 72 dp, ripple, y chevron `Abrir` a la derecha.

### `KpiTile`

| Parámetro | Tipo | Notas |
|---|---|---|
| icon | ImageVector | En círculo 48 dp, fondo `accentContainer` |
| label | String | `titleSmall`, 1-2 líneas ("Animales") |
| value | String | `displaySmall`, números tabulares ("124") |
| unit | String? | `bodyMedium` junto al valor ("litros") |
| caption | String? | `bodySmall` ("esta semana", "registrados") |
| accent | KpiAccent | `PRIMARY`, `TERTIARY`, `SECONDARY`, `ERROR` -> pares container/onContainer |
| onClick | (() -> Unit)? | Toda la tarjeta navegable, alto mín. 120 dp |
| state | KpiState | `Value`, `Loading` (placeholder gris del tamaño del valor), `Unavailable` (valor "-" + caption "Sin datos sin internet") |

Semántica combinada: "Animales, 124 registrados".

### `QuickActionButton(icon, label, onClick)`
Botón grande cuadrado/rectangular para accesos rápidos del Inicio: alto 96 dp, icono 32 dp arriba, `labelLarge` abajo, `surfaceContainerLowest` + borde. En grid de 2 o 3 columnas.

### `StatRow(icon, label, value)`
Fila de dato clave/valor para Detalle: icono 24 dp `onSurfaceVariant`, label `bodyMedium` `onSurfaceVariant`, valor `bodyLarge` Bold `onSurface` alineado a la derecha o debajo si no cabe (`FlowRow`). Alto mín. 56 dp, divisor `outlineVariant` entre filas.

### `ProgressBarRow(label, value, max, valueText, color)`
Barra horizontal simple para Finanzas/Reportes (sin librería de gráficos): label arriba, barra 12 dp redondeada, valor numérico a la derecha. Accesible: `contentDescription = "$label: $valueText"`.

## 4. Listas

### `AnimalListItem(item: AnimalItemUi, onClick)`
```text
┌──────────────────────────────────────────────┐
│ (B)  Lucero                        [Hembra]  │  (B) = círculo 48dp con AgroIcons.Bovino
│      Vaca · Holstein                          │
│      #001 · 480 kg · Potrero 1    [Por enviar]│
│                                            >  │
└──────────────────────────────────────────────┘
```
(Los símbolos del esquema representan iconos de `AgroIcons`, no caracteres a mostrar.)
- Título `titleMedium` (nombre o "Vaca 001"), línea 2 `bodyMedium` tipo · raza, línea 3 `bodyMedium` `onSurfaceVariant` con código, peso, potrero.
- `SexChip` arriba a la derecha; `SyncStateChip` solo si pendiente.
- Alto mín. 88 dp, toda la fila clicable.

### `AgroListItem(leadingIcon, title, supporting, trailing?, onClick?)`
Genérico (potreros, producción, tareas, ajustes). `ListItem` de M3 con alto mín. 72 dp, icono líder en círculo 40 dp.

### `TaskListItem(titulo, tipo, vencimiento, referencia, urgencia)`
`urgencia`: `VENCIDA` (icono `Vencido`, texto "Venció el 10/10" en `error`), `HOY` ("Vence hoy", `warning`), `PRONTO` (<= 3 días, `VencePronto`), `NORMAL`. Calcular a partir de `vencimiento` (`yyyy-MM-dd`) vs. fecha local.

## 5. Formularios

### `AgroTextField`
`OutlinedTextField` con alto mín. 56 dp, `bodyLarge`, label siempre visible arriba (no solo placeholder), `leadingIcon` opcional, `supportingText` para ayuda o error, `isError`. Parámetros: `value, onValueChange, label, leadingIcon?, keyboardType, imeAction, error: String?, helper: String?, required: Boolean` (agrega " *" al label y "Obligatorio" al TalkBack), `suffix` (p. ej. "kg").

### `AgroDropdownField<T>(label, options: List<T>, selected: T?, onSelect, optionLabel, leadingIcon?, error?)`
`ExposedDropdownMenuBox`, menú con ítems de 56 dp. Sustituye los `Spinner` con "Seleccionar X": el estado vacío es `selected = null` con placeholder "Elige una opción".

### `ChoiceChipGroup<T>(options, selected, onSelect)`
Para opciones cortas y frecuentes (sexo, tipo de animal, periodo de reporte): `FilterChip` de alto 48 dp, `labelLarge`, icono check al seleccionar. Preferir a dropdown cuando hay <= 4 opciones: se ve todo de un vistazo y es más fácil con guantes.

### `DateField(label, value: LocalDate?, onChange, maxDate = hoy)`
Campo de solo lectura que abre `DatePickerDialog` M3 (modo entrada de texto disponible). Muestra `dd/MM/yyyy`; guarda `yyyy-MM-dd`.

### `NumberField`
`AgroTextField` con `KeyboardType.Decimal`, acepta coma o punto, normaliza a `Double`, sufijo de unidad.

### `PrimaryButton(text, onClick, icon?, loading = false, enabled = true)`
`Button` ancho completo, alto 56 dp, forma `extraLarge`, `labelLarge`. `loading` muestra `CircularProgressIndicator` 20 dp y deshabilita. `SecondaryButton` = `OutlinedButton` mismas medidas. `TertiaryButton` = `TextButton` 48 dp.

### `FormSection(title, content)`
Agrupa campos con `titleMedium` y 16 dp entre campos.

## 6. Chips e insignias

- `SexChip(sexo)`: "Hembra" -> icono `Hembra`, color `female`; "Macho" -> `Macho`, `male`. Alto 32 dp, `labelMedium`.
- `StatusChip(text, tone: Tone)`: tonos `NEUTRAL`, `SUCCESS`, `WARNING`, `ERROR`, `INFO`. Para `PotreroLocal.estado` y `PendingTaskLocal.status`.
- `FilterChipsRow`: `LazyRow` de `FilterChip` 48 dp con padding horizontal 16 dp.
- `CountBadge(n)`: en barra inferior (Pendientes), `Badge` M3; "99+" si > 99.

## 7. Estados de pantalla

Todas las pantallas con datos usan `ScreenStateContainer(state: ContentState, ...)` con `ContentState = Loading | Empty | Error | Content`.

| Componente | Diseño |
|---|---|
| `LoadingState` | Primera carga: 3-5 `SkeletonListItem` (bloques `surfaceContainerHigh`, sin shimmer). Recargas: solo `LinearProgressIndicator` superior, el contenido previo permanece. |
| `EmptyState(icon, title, message, actionLabel?, onAction?)` | Centrado, icono 48 dp en círculo 96 dp `surfaceContainerHigh`, `titleMedium`, `bodyMedium` máx. 2 líneas, `PrimaryButton` opcional. Ej.: "Aún no hay animales" / "Registra tu primer animal para empezar." / "Agregar animal". |
| `ErrorState(title, message, onRetry)` | Igual que Empty con icono `Error` en `errorContainer`. Mensajes humanos: "No pudimos cargar los datos", nunca trazas técnicas. Botón "Reintentar". |
| `OfflineRequiredState(onRetry)` | Para funciones solo en línea (Reportes): icono `SinInternet`, "Necesitas internet para ver los reportes", "Reintentar". |

## 8. Navegación y acciones

### `AgroBottomBar(items, currentRoute, onSelect, badges: Map<String, Int>)`
Destinos fijos: Inicio, Animales, Producción, Potreros, Más (04 §2).
`NavigationBar` M3, alto 80 dp, fondo `surfaceContainer`, 5 ítems, **etiquetas siempre visibles** (`alwaysShowLabel = true`), icono 24 dp, indicador `primaryContainer`. Ver 04.

### `AgroFab(text, icon, onClick, expanded = true)`
`ExtendedFloatingActionButton` alto 64 dp, `primary`, texto siempre visible en pantallas de lista ("Agregar animal"); se contrae a icono solo al hacer scroll hacia abajo y se expande al subir. Posición: abajo a la derecha, 16 dp sobre la barra inferior.

### Diálogos
- `ConfirmDialog(title, message, confirmText, dismissText = "Cancelar", destructive = false)`: `AlertDialog` M3, botones de 48 dp, el destructivo en `error`. Usado en: cerrar sesión (si hay pendientes, advertir: "Tienes {n} cambios sin enviar. Si cierras sesión podrían perderse."), descartar formulario con cambios.
- `InfoDialog(title, message)`.
- No usar diálogos para éxito: usar `Snackbar`.

### Snackbar
`SnackbarHost` en `AgroScaffold`, `inverseSurface`, `bodyMedium`, duración `Long` para mensajes de guardado offline ("Animal guardado en el teléfono. Se enviará cuando haya internet."), acción opcional.

## 10. Gráficos: `AgroBarChart` (librería Vico)

Usado en: Historial de producción por animal (ficha 17), tab Producción del Detalle de animal (ficha 04), Finanzas (por semana) y Reportes.

### Librería recomendada: Vico (`com.patrykandpatrick.vico:compose-m3`)
- Nativa de Compose, con artefacto Material 3 que toma colores/tipografía del `MaterialTheme` (respeta `AgroTheme` claro/oscuro sin configuración extra).
- Licencia Apache 2.0, mantenida activamente, sin dependencias de vistas (a diferencia de MPAndroidChart, basado en View y sin mantenimiento).
- Soporta barras, línea, scroll/zoom horizontal, marcadores al tocar y animaciones desactivables.
- Versión de referencia: 3.3.x (última estable a oct. 2026). **Verificar compatibilidad con Kotlin/Compose del proyecto**; si la 3.x exige un Kotlin mayor que 2.1.0, usar la última 2.x compatible o subir Kotlin junto con el plugin `kotlin-compose`. La API cambió entre 2.x y 3.x: el agente debe leer la documentación de la versión elegida (guide.vico.patrykandpatrick.com) antes de codificar.

### Contrato del wrapper (aislar Vico detrás de un composable propio)

```kotlin
data class BarPoint(val label: String, val value: Double)   // label: "Lu", "12/10", "Sem 2", "Oct"

@Composable
fun AgroBarChart(
    points: List<BarPoint>,
    unit: String,                     // "L"
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.tertiary,   // leche = tertiary/milk
    average: Double? = null,          // línea horizontal punteada "Promedio 22,4 L"
    highlightLast: Boolean = true,    // barra de hoy en color sólido, resto 70% alfa
    height: Dp = 220.dp,
    contentDescription: String,       // resumen para TalkBack
)
```

Especificación visual:
- Barras con radio superior 6 dp, ancho mínimo 12 dp, separación 6 dp. Con más de 14 barras (vista Mes) habilitar scroll horizontal y arrancar al final (más reciente).
- Eje Y: 4 marcas, números `formatNumero`, `bodySmall` `onSurfaceVariant`; líneas de guía `outlineVariant` 1 dp. Eje X: `labelMedium`.
- Línea de promedio: `secondary`, punteada, con etiqueta a la derecha.
- Al tocar una barra: marcador con "12/10 · 22 L" (`inverseSurface`). No requerir gesto: debajo del gráfico siempre hay una lista/resumen textual con los mismos datos.
- Valores 0: barra de 2 dp para que se note el día sin registro; días sin datos se muestran como 0, no se omiten.
- Estados: `Loading` -> rectángulo `surfaceContainerHigh` del alto del gráfico; sin datos -> `EmptyState` compacto "Sin registros en este periodo".
- Accesibilidad: `Modifier.semantics { contentDescription = "Litros diarios, últimos 7 días: promedio 22 litros, máximo 26 el 12 de octubre" }`. Animación de entrada 300 ms, `snap()` si animaciones del sistema están desactivadas.

### `PeriodToggle(selected: Periodo, onSelect)`
`SingleChoiceSegmentedButtonRow` M3 con Semana / Mes / Año, alto 48 dp, ancho completo. (Figma: toggle Semana/Mes/Año.)

## 11. Componentes añadidos por el inventario Figma

| Componente | Uso | Especificación |
|---|---|---|
| `HomeHeader(saludo, subtitulo, pendientesBadge, onCampana, onPerfil)` | Inicio | Saludo `headlineMedium`, `IconButton` 48 dp con `AgroIcons.Notificaciones` (`Icons.Rounded.Notifications`) + `BadgedBox`, y `AgroIcons.Usuario` (perfil). cd: "Pendientes, 3 por atender", "Perfil y ajustes" |
| `AnimalAvatar(fotoUri?, codigo, size)` | Listas, detalle, producción | Si hay foto: `AsyncImage`/`BitmapFactory` recortado en círculo (lista 56 dp) o 16:9 forma `medium` (detalle). Si no: círculo `primaryContainer` con `Bovino`. Nunca imagen de stock |
| `PhotoPickerField(fotoUri?, onPick, onRemove)` | Formulario animal | Área 16:9 punteada `outline`, icono `Icons.Rounded.AddAPhoto` 32 dp + "Agregar foto"; bottom sheet con "Tomar foto" / "Elegir de la galería" (ítems 56 dp); con foto: miniatura + "Cambiar" / "Quitar" |
| `MilkingCard(animal, manana, tarde, total, onVerHistorial, onRegistrar)` | Producción de leche | `AgroCard`: avatar + "Lucero · 001", fila de 3 valores (Mañana / Tarde / Total) con `titleMedium` y unidad "L"; jornada sin registro muestra "-" y botón "Registrar" 48 dp |
| `ReproStatusChip(status)` | Reproducción, detalle | VACIA -> NEUTRAL "Vacía"; EN_CELO -> WARNING "En celo" (`Reproduccion`); INSEMINADA -> INFO "Inseminada"; GESTANTE -> SUCCESS "Gestante" (`Cria`); PARIDA -> INFO "Parida". Siempre icono + texto |
| `TimelineItem(icon, title, date, detail?)` | Eventos reproductivos, trabajos de potrero, ocupación | Línea vertical `outlineVariant` 2 dp, nodo circular 32 dp con icono, título `titleSmall`, fecha `bodySmall` |
| `AgroTabRow(tabs, selected, onSelect)` | Detalle animal | `PrimaryTabRow`, tabs alto 48 dp, `labelLarge`, indicador `primary` 3 dp |
| `WorkTypeChips` | Registrar trabajo | `ChoiceChipGroup` con iconos: Fumigar (`Icons.Rounded.Sanitizer`), Abonar (`Icons.Rounded.Compost`), Limpiar (`Icons.Rounded.CleaningServices`), Revisar pasto (`Grass`), Otro (`Icons.Rounded.MoreHoriz`); `FlowRow`, chips 48 dp |
| `JornadaSelector` | Registrar producción | Segmented Mañana (`Icons.Rounded.WbSunny`) / Tarde (`Icons.Rounded.WbTwilight`), 56 dp |
| `WelcomeHero` | Bienvenida | Ver ficha 13 |

Agregar a `AgroIcons` (01 §7.3): `Notificaciones`, `AgregarFoto`, `Fumigar`, `Abonar`, `Limpiar`, `Otro`, `Manana`, `Tarde`, `Gestante` (= `Cria`).

## 9. Lista de verificación de componentes

- [ ] Todos los componentes en `ui/components/` con previews.
- [ ] Ningún componente usa `Icons.*` directo, solo `AgroIcons`.
- [ ] Ningún componente define colores hex.
- [ ] Objetivos táctiles verificados con `Modifier.minimumInteractiveComponentSize()`.
