# 02 - Migración de XML a Jetpack Compose (Material 3)

Objetivo: pasar de 13 Activities + XML + View Binding a **una sola Activity** con Compose y Navigation Compose, **sin cambiar la lógica de negocio**. Las capas `data/`, `sync/`, `network/`, `auth/` y `AgroControlApp` se reutilizan. Solo se permiten las adiciones marcadas como **Adición permitida**.

## 1. Estado de partida (verificado en el código)

- `app/build.gradle.kts`: AGP 9.x (Kotlin integrado, sin plugin `kotlin-android` aplicado), KSP para Room, `viewBinding = true`, minSdk 24, target/compileSdk 37, Java 11.
- `gradle/libs.versions.toml`: kotlin `2.1.0`, lifecycle `2.8.7`, activityKtx `1.13.0`, room `2.7.0`, work `2.10.0`.
- Dependencias de servicios: `AgroControlApp` expone `tokenStore`, `syncScheduler`, `syncManager`, `animalRepository`. `database` es `private` (Activities usan `AppDatabase.get(context)` directamente).
- Tema XML: `Theme.Material3.DayNight.NoActionBar` sin personalizar.

## 2. Dependencias Gradle

### 2.1 `gradle/libs.versions.toml` (agregar)

```toml
[versions]
composeBom = "<última estable>"          # verificar en developer.android.com/develop/ui/compose/bom
activityCompose = "1.13.0"               # igual que activityKtx
navigationCompose = "<última estable 2.8+/2.9+>"
lifecycleCompose = "2.8.7"               # igual que lifecycle

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleCompose" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycleCompose" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version = "<3.3.x o compatible; ver 03 §10>" }
androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version = "1.0.1" }

[plugins]
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

Notas para el agente:
- La versión del plugin `kotlin-compose` **debe ser igual** a la de Kotlin (`2.1.0`). Si se sube Kotlin, sube ambos.
- Si el BOM más reciente exige Kotlin/AGP más nuevos, elegir el BOM compatible más reciente en vez de subir el toolchain.
- No usar navegación type-safe con `kotlinx.serialization` en esta fase (evita otro plugin); se usan rutas string (ver 04).

### 2.2 `build.gradle.kts` (raíz)

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

### 2.3 `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.compose)
}

android {
    buildFeatures {
        compose = true
        viewBinding = true   // temporal; se pone en false en el paso 13
    }
    buildTypes {
        release {
            optimization { enable = true }  // R8: recorta material-icons-extended
        }
    }
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.vico.compose.m3)   // gráficos (03 §10)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    // mantener todas las dependencias existentes (Room, Retrofit, Work, security-crypto...)
}
```

Al activar `optimization` en release, verificar reglas para Gson/Retrofit: agregar `proguard-rules.pro` con `-keep class com.jeison.agrocontrol.network.** { *; }` y `-keep class com.jeison.agrocontrol.data.** { *; }` (Gson usa reflexión sobre los DTO).

Criterio del paso 1: `./gradlew :app:assembleDebug` compila con la app actual intacta.

## 3. Arquitectura objetivo

```text
MainActivity (ComponentActivity, única)
 └─ AgroTheme
     └─ AgroApp()                       // Scaffold raíz + barra inferior + banner sync
         └─ AgroNavHost(navController)
             ├─ LoginRoute  -> LoginViewModel
             ├─ InicioRoute -> InicioViewModel
             ├─ ...Route    -> ...ViewModel  -> repositorios / DAOs existentes
```

- Flujo de datos unidireccional: `ViewModel` expone `StateFlow<XxxUiState>`; la pantalla llama lambdas (`onEvent`), el ViewModel llama al repositorio existente.
- Sin Hilt/Koin en esta fase: inyección manual con un `AppContainer` (menos piezas nuevas para el proyecto).
- `LoginActivity` deja de ser launcher; `MainActivity` pasa a ser la única Activity exportada.

## 4. Archivos del tema (`ui/theme/`)

| Archivo | Contenido |
|---|---|
| `Color.kt` | `val` por cada hex de 01 §2.1-2.2, `LightColors = lightColorScheme(...)`, `DarkColors = darkColorScheme(...)`, `AgroExtendedColors` claro/oscuro |
| `Type.kt` | `AtkinsonFamily` (`FontFamily(Font(R.font.atkinson_regular), Font(R.font.atkinson_medium, FontWeight.Medium), Font(R.font.atkinson_bold, FontWeight.Bold))`) y `AgroTypography` con la escala de 01 §3 |
| `Shape.kt` | `AgroShapes = Shapes(extraSmall = RoundedCornerShape(6.dp), ...)` |
| `Spacing.kt` | `object AgroSpacing`, `object AgroSizes` (01 §4) |
| `AgroIcons.kt` | `object AgroIcons` (01 §7.3) |
| `Theme.kt` | `AgroTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content)`; `dynamicColor` NO se usa; provee `LocalAgroColors`; `object AgroTheme { val colors @Composable get() = LocalAgroColors.current }`; configura barras del sistema con `enableEdgeToEdge()` en la Activity |

`ThemeMode { SYSTEM, LIGHT, DARK }` se guarda en `SharedPreferences` normales `agrocontrol_ui_prefs` clave `theme_mode` (preferencia de UI, no dato de negocio).

Tema XML: dejar `Theme.AgroControl` como padre `Theme.Material3.DayNight.NoActionBar` (solo afecta ventana previa); añadir `Theme.AgroControl.Splash` con `Theme.SplashScreen`, `windowSplashScreenBackground = #2E6B30` y el icono de la app.

## 5. Estructura de paquetes

```text
com.jeison.agrocontrol
├─ AgroControlApp.kt            (existente; + AppContainer, ver §6)
├─ MainActivity.kt              (reescrita, única Activity)
├─ auth/ data/ network/ sync/   (existentes, NO modificar salvo adiciones permitidas)
├─ di/AppContainer.kt
├─ ui/
│  ├─ AgroApp.kt                (Scaffold raíz, NavigationBar, SyncStatusBanner)
│  ├─ navigation/ AgroDestinations.kt, AgroNavHost.kt
│  ├─ theme/  (§4)
│  ├─ components/ (03-components.md, un archivo por familia)
│  ├─ common/ ConnectivityObserver.kt, SyncStatusRepository.kt, Formatters.kt, UiText.kt
│  └─ feature/
│     ├─ login/        LoginScreen.kt, LoginViewModel.kt, LoginUiState.kt
│     ├─ inicio/       ...
│     ├─ animales/     lista/, detalle/, formulario/
│     ├─ potreros/ produccion/ reproduccion/ finanzas/ pendientes/ reportes/ mas/
```

## 6. Infraestructura de UI (sin tocar lógica)

### 6.1 `AppContainer`

```kotlin
class AppContainer(private val app: AgroControlApp) {
    val database: AppDatabase get() = AppDatabase.get(app)   // mismo singleton que hoy
    val tokenStore get() = app.tokenStore
    val syncManager get() = app.syncManager
    val syncScheduler get() = app.syncScheduler
    val animalRepository get() = app.animalRepository
    val localCacheDao get() = database.localCacheDao()
    val syncDao get() = database.syncDao()
    val api: ApiService by lazy { RetrofitClient.createApi(app.tokenStore) }
    val connectivity by lazy { ConnectivityObserver(app) }
    val syncStatus by lazy { SyncStatusRepository(syncDao, connectivity) }
}
```

**Adición permitida** en `AgroControlApp`: `val container by lazy { AppContainer(this) }`. Nada más cambia en esa clase.

### 6.2 ViewModels con fábrica

```kotlin
val AppViewModelFactory = viewModelFactory {
    initializer { InicioViewModel(agroApp().container) }
    initializer { AnimalesListaViewModel(agroApp().container.animalRepository) }
    initializer { AnimalDetalleViewModel(createSavedStateHandle(), agroApp().container) }
    // ... uno por pantalla
}
fun CreationExtras.agroApp() = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as AgroControlApp
```

Uso en un Route: `val vm: InicioViewModel = viewModel(factory = AppViewModelFactory)`.

### 6.3 `ConnectivityObserver`

`ConnectivityManager.registerDefaultNetworkCallback` expuesto como `val isOnline: StateFlow<Boolean>` (`callbackFlow` + `stateIn(SharingStarted.WhileSubscribed(5000))`). Valor inicial: `activeNetwork` con `NET_CAPABILITY_VALIDATED`. Requiere `ACCESS_NETWORK_STATE` en el manifest (**adición permitida**, solo permiso).

### 6.4 `SyncStatusRepository` (solo lectura + disparo)

Expone `StateFlow<SyncStatus>`:

```kotlin
data class SyncStatus(
    val isOnline: Boolean,
    val pendingCount: Int,            // syncDao.pendingOutbox().size
    val lastPullIso: String?,         // syncDao.getMetadata("last_pull")?.value
    val isSyncing: Boolean,           // WorkManager getWorkInfosForUniqueWorkFlow("agrocontrol-sync") RUNNING, o sync manual en curso
    val lastError: Boolean,           // último syncAll() manual devolvió failure
)
suspend fun syncNow(): Result<Unit>   // llama a SyncManager.syncAll() existente y refresca
```

Como los DAO actuales son `suspend` (no `Flow`), refrescar `pendingCount`/`lastPull`: al iniciar, al volver a primer plano (`Lifecycle.Event.ON_RESUME` vía `LifecycleEventEffect`), tras cada guardado y cuando WorkManager pase a `SUCCEEDED`. No convertir los DAO a `Flow` en esta fase (sería cambio de capa de datos); opcional como mejora futura.

### 6.5 `Formatters`

- `formatFechaCorta("2020-03-15") -> "15/03/2020"` (fallback: devolver texto original si no parsea).
- `formatEdad(fechaNacimiento) -> "4 años 2 meses"` / `"8 meses"` / `"12 días"` (`java.time` requiere desugaring con minSdk 24: activar `isCoreLibraryDesugaringEnabled = true` + `coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.x")`. Nota: `SyncManager` ya usa `java.time.Instant`, por lo que el desugaring también le beneficia).
- `formatNumero(480.0) -> "480"`, `formatNumero(12.5) -> "12,5"` con `Locale("es","CO")`.
- `formatHaceCuanto(iso) -> "hace 5 min" / "hoy 08:30" / "ayer" / "12/10/2026"`.

## 7. Patrón ViewModel / UiState

Plantilla obligatoria (ejemplo: lista de animales):

```kotlin
data class AnimalesListaUiState(
    val query: String = "",
    val filtroTipo: String? = null,
    val animales: List<AnimalItemUi> = emptyList(),
    val totalLocal: Int = 0,
    val isLoading: Boolean = true,
    val error: UiText? = null,
)

data class AnimalItemUi(
    val codigo: String, val titulo: String, val tipo: String, val raza: String,
    val sexo: String, val pesoTexto: String, val potrero: String, val pendienteSync: Boolean,
)

class AnimalesListaViewModel(private val repo: AnimalRepository) : ViewModel() {
    private val _state = MutableStateFlow(AnimalesListaUiState())
    val state: StateFlow<AnimalesListaUiState> = _state.asStateFlow()
    private var searchJob: Job? = null

    init { cargar() }

    fun onQueryChange(q: String) {
        _state.update { it.copy(query = q) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch { delay(250); cargar() }   // debounce
    }

    fun cargar() = viewModelScope.launch {
        runCatching { repo.search(_state.value.query) }          // método existente
            .onSuccess { list -> _state.update { it.copy(isLoading = false, animales = list.map(Animal::toItemUi), error = null) } }
            .onFailure { _state.update { it.copy(isLoading = false, error = UiText.Res(R.string.error_cargar_animales)) } }
    }
}

// Mapper en la capa UI (NO usar Animal.tituloDisplay(): contiene emoji)
fun Animal.toItemUi() = AnimalItemUi(
    codigo = codigo,
    titulo = nombre.ifBlank { "$tipo $codigo" },
    tipo = tipo, raza = raza, sexo = sexo,
    pesoTexto = "${formatNumero(peso)} kg",
    potrero = potrero,
    pendienteSync = syncState == Animal.SyncState.PENDING.name,
)
```

Route + Screen:

```kotlin
@Composable
fun AnimalesListaRoute(onAbrir: (String) -> Unit, onAgregar: () -> Unit,
                       vm: AnimalesListaViewModel = viewModel(factory = AppViewModelFactory)) {
    val state by vm.state.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { vm.cargar() }
    AnimalesListaScreen(state, vm::onQueryChange, onAbrir, onAgregar, onReintentar = vm::cargar)
}

@Composable
fun AnimalesListaScreen(state: AnimalesListaUiState, /* lambdas */) { /* stateless */ }
```

Reglas:
- Pantallas stateless y con `@Preview` (claro, oscuro, `fontScale = 1.5f`) usando `UiState` de muestra.
- Mensajes efímeros (guardado, error de sync) vía `Channel<UiEvent>` consumido con `LaunchedEffect` -> `SnackbarHostState`.
- Llamadas de red siempre dentro de `runCatching`; nunca mostrar `exception.message` crudo al usuario (mapear a textos de 01 §1).
- Lógica que hoy está en Activities se **traslada** a ViewModels sin cambiarla (mismas validaciones, mismos métodos de repositorio/DAO/API).

## 8. Migración paso a paso por pantalla

Para cada pantalla: (1) crear `UiState` + `ViewModel` trasladando la lógica de la Activity; (2) crear `Screen` con componentes de 03; (3) registrar la ruta en `AgroNavHost`; (4) quitar el `Intent`/`startActivity` que apuntaba a la Activity vieja; (5) borrar Activity + layout cuando ninguna referencia quede.

| Activity actual | Lógica a trasladar | Destino Compose | Fuente de datos |
|---|---|---|---|
| `LoginActivity` | `login()`: validar no vacío, `api.login`, `tokenStore.saveTokens`, `syncManager.syncAll()`, ir a inicio. `register()` | `feature/login` | `ApiService.login/register`, `TokenStore` |
| `MainActivity` (dashboard) | `actualizarDashboard()`: `animalRepository.count()`, `api.dashboardSummary()`; `syncAll()` en onResume | `feature/inicio` | ídem + `LocalCacheDao` como respaldo offline |
| `AnimalesActivity` | `repository.search(query)`, abrir detalle, abrir alta | `feature/animales/lista` | `AnimalRepository.search` |
| `DetalleAnimalActivity` | Lee extras del Intent | `feature/animales/detalle` | **Cambia la fuente**: ruta con `codigo` y `AnimalDao.findByCodigo(codigo)` (método existente) |
| `AgregarAnimalActivity` | `guardarAnimal()`: mismas validaciones, `repository.insert`, manejo de código duplicado | `feature/animales/formulario` | `AnimalRepository.insert`, `LocalCacheDao.allPotreros()` |
| `PotrerosActivity` | `allPotreros()` | `feature/potreros` | `LocalCacheDao.allPotreros` |
| `ProduccionActivity` | `allProduction()` | `feature/produccion` | `LocalCacheDao.allProduction` |
| `PendientesActivity` | `pendingTasks()` | `feature/pendientes` | `LocalCacheDao.pendingTasks` (+ `api.tasks()` al refrescar con red) |
| `FinanzasActivity` | agrupación `groupBy(productType).sumOf(cantidad)` | `feature/finanzas` | `LocalCacheDao.allProduction` |
| `ReportesActivity` | `reportWeekly(from,to)` y `reportMonthly(year,month)` | `feature/reportes` | `ApiService` (requiere red) |
| `ReproduccionActivity` | Texto estático | `feature/reproduccion` | Sin fuente local; KPI `DashboardSummary.eventosReproduccion` |
| `MasActivity` | `syncNow()`, `cerrarSesion()` (`tokenStore.clear()`) | `feature/mas` | `SyncManager`, `TokenStore` |
| `ModuleListActivity` | Base genérica | Se elimina (reemplazada por componentes) | - |

Correcciones obligatorias durante la migración (bugs del código actual, no cambian reglas de negocio):
- Reportes: el código actual arma fechas sin ceros (`2026-1-5`). Usar `LocalDate.toString()` (`yyyy-MM-dd`), que es el formato del contrato (`docs/patterns/api-contracts.md`).
- Detalle: no usar valores por defecto inventados (`"Holstein"`, `450.0`); si no existe el animal mostrar estado de error.

### Adiciones permitidas a la capa de datos (solo paso 12, edición)

`AnimalRepository.update(animal: Animal)` que replica el patrón de `insert`: `animalDao.update(animal.copy(syncState = PENDING, updatedAtEpoch = now))`, encolar `SyncOutbox(entity="ANIMAL", operation="UPDATE", localId=id, serverId=animal.serverId, payloadJson=<mismo mapa que insert>)` y `syncScheduler?.schedule()`. **Antes de implementar, confirmar que el backend acepta `operation = "UPDATE"` en `/sync/push`**; si no, dejar el botón Editar oculto tras un flag `FeatureFlags.EDITAR_ANIMAL = false`.

## 9. Activity única y eliminación de XML (paso 13)

1. `MainActivity : ComponentActivity`:
   ```kotlin
   override fun onCreate(savedInstanceState: Bundle?) {
       installSplashScreen()
       super.onCreate(savedInstanceState)
       enableEdgeToEdge()
       val container = (application as AgroControlApp).container
       setContent {
           val themeMode by rememberThemeMode()
           AgroTheme(themeMode) {
               AgroApp(startDestination = if (container.tokenStore.isLoggedIn()) Graph.MAIN else Graph.AUTH)
           }
       }
   }
   ```
2. `AndroidManifest.xml`: `MainActivity` con `exported="true"`, intent-filter MAIN/LAUNCHER, `windowSoftInputMode="adjustResize"`, `android:theme="@style/Theme.AgroControl.Splash"`. Eliminar todas las demás `<activity>`.
3. Borrar: `LoginActivity`, `AnimalesActivity`, `AgregarAnimalActivity`, `DetalleAnimalActivity`, `FinanzasActivity`, `MasActivity`, `ModuleListActivity`, `PendientesActivity`, `PotrerosActivity`, `ProduccionActivity`, `ReportesActivity`, `ReproduccionActivity`.
4. Borrar `res/layout/*` (`activity_*.xml`, `item_animal_card.xml`, `activity_animales2.xml`).
5. `viewBinding = false`; quitar `androidx.constraintlayout` y `androidx.appcompat` si ya nadie los usa (`material` XML puede quedarse por el tema de ventana o reemplazarse por `Theme.Material3` de la misma lib).
6. Mover textos a `res/values/strings.xml` en español (UTF-8).
7. No eliminar ni modificar: `Animal.tituloDisplay()` / `detalleLineas()` pueden quedar sin uso (son de `data/`); marcarlos `@Deprecated("Usar mapper UI")` es opcional y no cambia comportamiento.
8. Actualizar `docs/DESIGN_FIGMA_MAPPING.md` y `Memoria.md` (decisión "Móvil": Compose + Material 3).

## 10. Pruebas

- Unit: mappers (`Animal.toItemUi`), `Formatters`, validación del formulario (`AnimalFormValidator` puro, extraído del ViewModel con las mismas reglas que `guardarAnimal()`).
- UI (`createComposeRule`): Login vacío muestra errores; formulario de animal guarda y vuelve a la lista; banner offline aparece con `isOnline = false`.
- Manual: `docs/architecture/e2e-smoke.md` + modo avión + brillo al máximo en exterior.

## 11. Feature flags (pantallas con dependencia de backend del Figma)

Crear `ui/common/FeatureFlags.kt`:

```kotlin
object FeatureFlags {
    const val EDITAR_ANIMAL = false        // AnimalRepository.update + sync UPDATE
    const val FOTOS_ANIMAL = false         // campo fotoUri + subida
    const val RECUPERAR_CLAVE = false      // POST /auth/forgot-password
    const val CREAR_POTRERO = false        // sync entity POTRERO + campos nuevos
    const val REGISTRAR_TRABAJO = false    // sync entity TASK (create)
    const val REGISTRAR_PRODUCCION = false // sync entity PRODUCTION + animalCodigo + jornada
    const val PRODUCCION_POR_ANIMAL = false
    const val REPRODUCCION = false         // endpoint + Room reproduction_events + sync
}
```

Regla: la UI de cada pantalla se construye y se previsualiza completa con datos de ejemplo, pero sus puntos de entrada se ocultan mientras el flag sea `false`. Activar un flag requiere que el agente de persistencia haya añadido la entidad/DAO/outbox correspondiente siguiendo el patrón de `AnimalRepository.insert` (Room + `SyncOutbox` + `syncScheduler.schedule()`), y que el backend acepte la entidad en `/sync/push` y la devuelva en `/sync/pull`. Lista completa en 00-README §6.3.

Permisos nuevos (solo cuando `FOTOS_ANIMAL = true`): ninguno para `PickVisualMedia`; `TakePicture` con `FileProvider` (sin permiso de cámara si se usa el intent del sistema).

Bienvenida: preferencia `agrocontrol_ui_prefs/onboarding_visto` (Boolean) [UI].
