# 01 - Sistema de diseño "Campo" (AgroControl)

Implementar en `app/src/main/java/com/jeison/agrocontrol/ui/theme/`. Ningún composable usa valores hex, `sp` o `dp` sueltos: todo sale de este documento.

## 1. Dirección de marca

- **Personalidad**: confiable, sobria, de trabajo. Una herramienta como un buen machete o una báscula: robusta, clara, sin adornos.
- **Inspiración cromática**: verde de potrero (acción y marca), tierra/cuero (acento cálido), azul de agua/cielo (información, leche), sobre fondos ligeramente cálidos (no blanco puro, reduce reflejo).
- **Tono de voz**: tú, frases cortas, verbos en infinitivo en botones ("Guardar", "Sincronizar"), sin tecnicismos ("Guardado en el teléfono" en vez de "Persistido localmente"; "Sin internet" en vez de "Offline").
- **Lo que se evita**: pasteles de baja saturación (los KPI actuales `#E7FF80`, `#A8D9FF` se pierden al sol), degradados, sombras fuertes, texto gris claro, iconos sin etiqueta.

## 2. Color

Se usa `ColorScheme` de Material 3 completo. **Dynamic color (Material You) DESACTIVADO**: la marca y el contraste deben ser constantes en cualquier teléfono.

### 2.1 Esquema claro (`LightColors`)

| Rol M3 | Hex | Uso |
|---|---|---|
| primary | `#2E6B30` | Botones primarios, FAB, ítem activo de barra inferior, enlaces |
| onPrimary | `#FFFFFF` | Texto/icono sobre primary (contraste 6.6:1) |
| primaryContainer | `#B5F1AB` | Fondo de chips seleccionados, indicador de barra inferior |
| onPrimaryContainer | `#002106` | |
| secondary | `#77592A` | Acento tierra: potreros, finanzas |
| onSecondary | `#FFFFFF` | |
| secondaryContainer | `#FFDDB2` | |
| onSecondaryContainer | `#291800` | |
| tertiary | `#1E6587` | Información, leche, reportes |
| onTertiary | `#FFFFFF` | |
| tertiaryContainer | `#C5E7FF` | |
| onTertiaryContainer | `#001E2C` | |
| error | `#B3261E` | Errores, vencido |
| onError | `#FFFFFF` | |
| errorContainer | `#F9DEDC` | |
| onErrorContainer | `#410E0B` | |
| background | `#F8F9F2` | Fondo de pantallas |
| onBackground | `#1A1C18` | |
| surface | `#F8F9F2` | |
| onSurface | `#1A1C18` | Texto principal (contraste 16:1) |
| surfaceVariant | `#DEE5D8` | |
| onSurfaceVariant | `#3F4A3C` | Texto secundario (mín. 8:1; nunca usar gris más claro) |
| surfaceContainerLowest | `#FFFFFF` | Tarjetas sobre fondo |
| surfaceContainerLow | `#F2F4EC` | |
| surfaceContainer | `#ECEFE6` | Barra inferior |
| surfaceContainerHigh | `#E6E9E0` | Campos de texto rellenos, diálogos |
| surfaceContainerHighest | `#E1E4DA` | |
| outline | `#6F7A6B` | Bordes de campos y tarjetas (3:1 mín.) |
| outlineVariant | `#BFC9BA` | Divisores |
| inverseSurface | `#2F312C` | Snackbar |
| inverseOnSurface | `#F0F1E9` | |
| inversePrimary | `#9AD591` | |
| scrim | `#000000` | |

### 2.2 Esquema oscuro (`DarkColors`)

| Rol M3 | Hex |
|---|---|
| primary | `#9AD591` |
| onPrimary | `#003910` |
| primaryContainer | `#14521A` |
| onPrimaryContainer | `#B5F1AB` |
| secondary | `#E8C08A` |
| onSecondary | `#432C00` |
| secondaryContainer | `#5D4214` |
| onSecondaryContainer | `#FFDDB2` |
| tertiary | `#8ECFF5` |
| onTertiary | `#003548` |
| tertiaryContainer | `#004C67` |
| onTertiaryContainer | `#C5E7FF` |
| error | `#F2B8B5` |
| onError | `#601410` |
| errorContainer | `#8C1D18` |
| onErrorContainer | `#F9DEDC` |
| background | `#11140F` |
| onBackground | `#E1E4DA` |
| surface | `#11140F` |
| onSurface | `#E1E4DA` |
| surfaceVariant | `#3F4A3C` |
| onSurfaceVariant | `#C2CBBD` |
| surfaceContainerLowest | `#0C0F0A` |
| surfaceContainerLow | `#1A1C18` |
| surfaceContainer | `#1E211C` |
| surfaceContainerHigh | `#282B26` |
| surfaceContainerHighest | `#333631` |
| outline | `#8A9485` |
| outlineVariant | `#3F4A3C` |
| inverseSurface | `#E1E4DA` |
| inverseOnSurface | `#2F312C` |
| inversePrimary | `#2E6B30` |

### 2.3 Colores semánticos extendidos (`AgroExtendedColors`)

M3 no trae "éxito/advertencia". Crear `data class AgroExtendedColors` y proveerla con `staticCompositionLocalOf` (`LocalAgroColors`), accesible como `AgroTheme.colors`.

| Token | Claro | Oscuro | Uso |
|---|---|---|---|
| success | `#1F6E2B` | `#8FD88F` | "Sincronizado", tarea completada |
| onSuccess / successContainer | `#FFFFFF` / `#C8EFC4` | `#00390F` / `#145220` | |
| warning | `#7A4F00` | `#FFB95C` | "Pendiente de enviar", vence hoy |
| onWarning / warningContainer | `#FFFFFF` / `#FFDDB8` | `#422C00` / `#5E3F00` | |
| offline | `#4A4F47` | `#C2C8BE` | Banner "Sin internet" |
| offlineContainer | `#E1E4DA` | `#333631` | |
| milk (leche) | `#1E6587` = tertiary | `#8ECFF5` | Producción de leche |
| meat (carne) | `#8C3B2E` | `#FFB4A5` | Producción de carne |
| female / male | `#8B3F6B` / `#2B5C8A` | `#FFAFD8` / `#A3C9FF` | Chip sexo (siempre con icono y texto, nunca solo color) |

Los KPI del Inicio NO usan cinco pasteles distintos: usan `surfaceContainerLowest` con un icono en un círculo del color de acento del módulo (primary, tertiary, secondary, error para alertas). Esto mantiene contraste al sol.

### 2.4 Reglas de color

- Texto sobre fondo: solo pares `onX` sobre `X`. Verificar con el plugin de contraste: texto normal >= 7:1, texto grande y iconos >= 4.5:1, bordes de controles >= 3:1.
- El estado nunca se comunica solo con color: siempre icono + texto.
- Modo oscuro: se sigue el sistema por defecto, con opción manual en Más > Apariencia (Sistema / Claro / Oscuro). Para trabajo al sol se recomienda claro; mostrar esa nota en Ajustes.

## 3. Tipografía

Familia: **Atkinson Hyperlegible Next** (Braille Institute, licencia OFL), diseñada para máxima distinción de caracteres (1/l/I, 0/O), ideal para baja visión y lectura al sol. Empaquetar como recurso (`res/font/atkinson_regular.ttf`, `atkinson_medium.ttf`, `atkinson_bold.ttf`), **no** usar fuentes descargables (requieren red). Fallback: `FontFamily.Default` (Roboto). Para cifras en KPI usar `fontFeatureSettings = "tnum"` (números tabulares).

Escala (`Type.kt`, sobrescribe `Typography` de M3; tamaños mayores que el estándar):

| Estilo M3 | Tamaño / interlineado | Peso | Uso |
|---|---|---|---|
| displaySmall | 36 / 44 sp | Bold | Valor KPI destacado |
| headlineMedium | 28 / 36 sp | Bold | Título de pantalla grande (Inicio, Login) |
| headlineSmall | 24 / 32 sp | Bold | Nombre del animal en Detalle |
| titleLarge | 22 / 28 sp | Bold | Título de TopAppBar |
| titleMedium | 18 / 24 sp | Bold | Título de tarjeta, título de sección |
| titleSmall | 16 / 22 sp | Medium | Subtítulo de tarjeta |
| bodyLarge | 18 / 26 sp | Regular | Texto de lectura, valores de formulario |
| bodyMedium | 16 / 24 sp | Regular | Texto por defecto |
| bodySmall | 14 / 20 sp | Regular | Texto auxiliar (mínimo absoluto permitido) |
| labelLarge | 16 / 20 sp | Bold | Botones |
| labelMedium | 14 / 18 sp | Medium | Chips, etiquetas de barra inferior |
| labelSmall | 13 / 16 sp | Medium | Solo insignias (badge); no usar para información |

Reglas: nada por debajo de 13 sp; respetar escalado de fuente del sistema hasta 200% (no fijar alturas de texto; usar `heightIn(min=)`); máximo 2 pesos por tarjeta.

## 4. Espaciado (`Spacing.kt`)

Grid base de 4 dp. Exponer `object AgroSpacing`:

| Token | dp | Uso |
|---|---|---|
| xxs | 4 | Separación icono-texto interna |
| xs | 8 | Entre chips, entre líneas de metadatos |
| sm | 12 | Padding interno de chips grandes |
| md | 16 | Padding de tarjeta, margen horizontal de pantalla |
| lg | 24 | Entre secciones |
| xl | 32 | Separación de bloques en Login |
| xxl | 48 | Márgenes superiores de estados vacíos |

Tamaños táctiles (`object AgroSizes`): `touchMin = 48.dp`, `touchPrimary = 56.dp` (botones primarios, campos de texto, ítems de lista), `fab = 64.dp` (FAB extendido alto 64), `iconSm = 20.dp`, `icon = 24.dp`, `iconLg = 32.dp`, `iconXl = 48.dp` (estados vacíos), `listItemMin = 72.dp`, `bottomBar = 80.dp`.

## 5. Formas (`Shape.kt`)

| Token M3 | Radio | Uso |
|---|---|---|
| extraSmall | 6 dp | Badges |
| small | 10 dp | Chips, campos de texto |
| medium | 14 dp | Tarjetas, KPI |
| large | 20 dp | Bottom sheets (superior), diálogos |
| extraLarge | 28 dp | FAB extendido, botones primarios (forma píldora) |

## 6. Elevación

Al sol las sombras casi no se ven: la jerarquía se marca con **color de superficie + borde 1 dp `outlineVariant`**, no con sombra.

| Nivel | Uso | Implementación |
|---|---|---|
| 0 | Fondo, listas | `background` |
| 1 | Tarjetas | `surfaceContainerLowest` + borde 1 dp `outlineVariant`, `tonalElevation = 0`, `shadowElevation = 0` |
| 2 | Barra inferior, TopAppBar al hacer scroll | `surfaceContainer` |
| 3 | FAB, menús | `shadowElevation = 3.dp` |
| 4 | Diálogos, bottom sheets | `surfaceContainerHigh` + scrim 32% |

## 7. Iconografía

### 7.1 Librería elegida: Material Symbols vía `androidx.compose.material:material-icons-extended`, estilo **Rounded**

Justificación:
- Es el set oficial de Material 3: misma geometría, grosor y alineación óptica que los componentes M3 (NavigationBar, chips, campos).
- Se distribuye como `ImageVector` dentro del BOM de Compose: sin red, sin assets, sin dependencias de terceros, compatible offline.
- Cubre casi todos los conceptos del dominio (`Grass`, `Fence`, `Agriculture`, `WaterDrop`, `MonitorWeight`, `Female`, `Male`, `CloudOff`, `Sync`).
- Estilo Rounded: terminaciones redondeadas, más amables y legibles a tamaño pequeño que Outlined.
- Lucide/Phosphor se descartan: requieren dependencias externas y su trazo fino (1.5-2 px) pierde legibilidad al sol.

Costo: el artefacto extendido es grande (~decenas de MB sin reducir). **Obligatorio** habilitar R8 en release (`isMinifyEnabled = true` / `optimization { enable = true }`) para eliminar los iconos no usados. Alternativa si el APK de debug pesa demasiado: copiar solo los iconos usados como `ImageVector` a `ui/icons/` (generados desde fonts.google.com/icons, estilo Rounded, peso 400, grado 0, tamaño óptico 24).

Icono propio: no existe un icono de bovino en Material Symbols. Crear **un único** `ImageVector` `AgroIcons.Bovino` (cabeza de res frontal, grid 24 dp, área viva 20 dp, trazo 2 dp con terminaciones redondeadas, coherente con Rounded). Hasta que exista, usar `Icons.Rounded.Pets` como sustituto.

### 7.2 Regla de uso

Todos los iconos se referencian vía `object AgroIcons` en `ui/theme/AgroIcons.kt` (nunca `Icons.Rounded.X` directamente en pantallas). Así se cambia el set en un solo lugar. Iconos con significado siempre llevan `contentDescription` en español; iconos decorativos junto a texto usan `contentDescription = null`.

### 7.3 Mapeo concepto -> icono

| Concepto | `AgroIcons.` | Material Symbol (Rounded) |
|---|---|---|
| Inicio | `Inicio` | `Icons.Rounded.Home` |
| Animales / bovino | `Bovino` | propio (fallback `Icons.Rounded.Pets`) |
| Potreros | `Potrero` | `Icons.Rounded.Grass` |
| Cerca / ubicación de potrero | `Cerca` | `Icons.Rounded.Fence` |
| Área (hectáreas) | `Area` | `Icons.Rounded.Straighten` |
| Producción | `Produccion` | `Icons.Rounded.Agriculture` |
| Leche | `Leche` | `Icons.Rounded.WaterDrop` |
| Carne / peso | `Peso` | `Icons.Rounded.MonitorWeight` |
| Reproducción | `Reproduccion` | `Icons.Rounded.Favorite` |
| Parto / cría | `Cria` | `Icons.Rounded.ChildCare` |
| Finanzas / productividad | `Finanzas` | `Icons.Rounded.Payments` |
| Tendencia | `Tendencia` | `Icons.AutoMirrored.Rounded.TrendingUp` |
| Reportes | `Reportes` | `Icons.Rounded.BarChart` |
| Pendientes / tareas | `Pendientes` | `Icons.Rounded.TaskAlt` |
| Vencido | `Vencido` | `Icons.Rounded.ErrorOutline` |
| Vence pronto | `VencePronto` | `Icons.Rounded.Schedule` |
| Más | `Mas` | `Icons.Rounded.Menu` |
| Agregar | `Agregar` | `Icons.Rounded.Add` |
| Editar | `Editar` | `Icons.Rounded.Edit` |
| Buscar | `Buscar` | `Icons.Rounded.Search` |
| Borrar texto (búsqueda) | `BorrarTexto` | `Icons.Rounded.Close` |
| Filtrar | `Filtrar` | `Icons.Rounded.FilterList` |
| Volver | `Volver` | `Icons.AutoMirrored.Rounded.ArrowBack` |
| Ir / abrir | `Abrir` | `Icons.AutoMirrored.Rounded.KeyboardArrowRight` |
| Fecha | `Fecha` | `Icons.Rounded.CalendarMonth` |
| Código / arete | `Codigo` | `Icons.Rounded.Tag` |
| Raza | `Raza` | `Icons.Rounded.Category` |
| Hembra / Macho | `Hembra` / `Macho` | `Icons.Rounded.Female` / `Icons.Rounded.Male` |
| Observación | `Nota` | `Icons.Rounded.Notes` |
| Sincronizar | `Sincronizar` | `Icons.Rounded.Sync` |
| Sincronizado | `Sincronizado` | `Icons.Rounded.CloudDone` |
| Pendiente de enviar | `PorEnviar` | `Icons.Rounded.CloudUpload` |
| Sin internet | `SinInternet` | `Icons.Rounded.CloudOff` |
| Error | `Error` | `Icons.Rounded.Error` |
| Éxito | `Exito` | `Icons.Rounded.CheckCircle` |
| Correo | `Correo` | `Icons.Rounded.Mail` |
| Contraseña | `Clave` | `Icons.Rounded.Lock` |
| Mostrar / ocultar clave | `Ver` / `Ocultar` | `Icons.Rounded.Visibility` / `Icons.Rounded.VisibilityOff` |
| Usuario | `Usuario` | `Icons.Rounded.Person` |
| Finca | `Finca` | `Icons.Rounded.Cottage` |
| Apariencia | `Tema` | `Icons.Rounded.Contrast` |
| Cerrar sesión | `Salir` | `Icons.AutoMirrored.Rounded.Logout` |
| Información | `Info` | `Icons.Rounded.Info` |
| Notificaciones | `Notificaciones` | `Icons.Rounded.Notifications` |
| Agregar foto | `AgregarFoto` | `Icons.Rounded.AddAPhoto` |
| Fumigar | `Fumigar` | `Icons.Rounded.Sanitizer` |
| Abonar | `Abonar` | `Icons.Rounded.Compost` |
| Limpiar | `Limpiar` | `Icons.Rounded.CleaningServices` |
| Otro | `Otro` | `Icons.Rounded.MoreHoriz` |
| Jornada mañana / tarde | `Manana` / `Tarde` | `Icons.Rounded.WbSunny` / `Icons.Rounded.WbTwilight` |
| Celo | `Celo` | `Icons.Rounded.Whatshot` |
| Inseminación | `Inseminacion` | `Icons.Rounded.Vaccines` |
| Diagnóstico gestación | `Diagnostico` | `Icons.Rounded.FactCheck` |
| Estado del pasto | `Pasto` | `Icons.Rounded.Eco` |

Nota: `Limpiar` (cerrar búsqueda) y el trabajo "Limpiar" chocan de nombre: renombrar el de búsqueda a `BorrarTexto`.

Tamaños: 24 dp por defecto, 32 dp en KPI y accesos rápidos, 48 dp en estados vacíos/error. Contenedor circular de icono KPI: 48 dp con fondo `XContainer` e icono `onXContainer`.

## 8. Movimiento

Breve y funcional (teléfonos de gama baja, usuarios que no deben perderse).

| Caso | Spec |
|---|---|
| Navegación entre destinos de barra inferior | `fadeIn(tween(150))` / `fadeOut(tween(100))`, sin desplazamiento |
| Navegación a detalle / formulario | `slideInHorizontally { it / 4 } + fadeIn(tween(200, easing = FastOutSlowInEasing))`; regreso inverso |
| Aparición de banner de sync/offline | `expandVertically(tween(200)) + fadeIn` |
| Cambio de estado de sync (pendiente -> sincronizado) | `Crossfade(tween(200))` en icono y texto |
| Pulsación | ripple estándar M3 (no escalar botones) |
| Carga | `LinearProgressIndicator` indeterminado bajo TopAppBar; esqueletos sin shimmer (shimmer consume batería) |

Respetar "Quitar animaciones" del sistema: leer `Settings.Global.ANIMATOR_DURATION_SCALE`; si es 0, usar `snap()`.

## 9. Accesibilidad y uso en campo

Checklist obligatorio por pantalla:

- [ ] Objetivos táctiles >= 48 dp (acciones primarias 56 dp). Separación mínima 8 dp entre objetivos.
- [ ] Contraste según §2.4, verificado en claro y oscuro.
- [ ] Funciona con fuente al 200% sin cortar texto (probar `fontScale = 2f` en Preview).
- [ ] Todo icono interactivo con `contentDescription`; listas con `Modifier.semantics(mergeDescendants = true)` por ítem.
- [ ] Estados nunca solo por color.
- [ ] Teclado adecuado: `KeyboardType.Number`/`Decimal` en peso y cantidades, `Email` en correo; `ImeAction.Next` encadenado y `Done` en el último campo.
- [ ] Sin gestos ocultos como única vía (no depender de swipe-to-delete, long-press ni doble tap). Toda acción tiene botón visible.
- [ ] Acciones destructivas o irreversibles piden confirmación con diálogo y botones grandes.
- [ ] Formularios conservan datos al rotar o al ir a segundo plano (`SavedStateHandle` / `rememberSaveable`).
- [ ] TalkBack recorre en orden lógico; títulos con `Modifier.semantics { heading() }`.
- [ ] Textos simples: máximo ~8 palabras por botón o etiqueta.

## 10. Imágenes (inventario Figma)

El Figma usa una foto de ganado en el Splash y fotos de animales. Reglas:
- Bienvenida: una sola ilustración/foto propia de la finca o libre de derechos, optimizada (WebP, <= 150 KB, `res/drawable-nodpi`), con scrim `#000000` al 45% inferior para garantizar contraste del texto blanco. Sin texto incrustado en la imagen.
- Fotos de animales: solo las tomadas por el usuario (flag `FOTOS_ANIMAL`); nunca imágenes de stock como relleno. Sin foto -> `AnimalAvatar` con icono.
- Logo: isotipo vectorial (cabeza de res simplificada, misma geometría que `AgroIcons.Bovino`) + wordmark "AgroControl" en Atkinson Bold. El nombre "AgroGest" del Figma se considera nombre de trabajo; la app se llama AgroControl (`app_name`).
