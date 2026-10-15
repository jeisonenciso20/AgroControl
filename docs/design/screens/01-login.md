# Pantalla: Login y Registro

Ruta: `login`, `registro` (grafo `auth`). Reemplaza `LoginActivity` + `activity_login.xml`. Paquete `ui/feature/login`.

## Propósito
Entrar a la finca con correo y contraseña. Crear cuenta nueva (finca + usuario) de forma real, no con datos demo aleatorios.

## Wireframe - Login
```text
┌────────────────────────────────────┐
│                                    │
│        [logo AgroControl 72dp]     │
│           AgroControl              │  headlineMedium
│   Gestión de tu finca, aun sin     │  bodyLarge, onSurfaceVariant
│           internet                 │
│                                    │
│  Correo electrónico *              │
│  [ (Correo) tu@correo.com       ]  │  AgroTextField Email, ImeAction.Next
│                                    │
│  Contraseña *                      │
│  [ (Clave) ********      (Ver)  ]  │  ImeAction.Done -> enviar
│                                    │
│  [ error general si aplica      ]  │  tarjeta errorContainer
│                                    │
│  [        Ingresar             ]   │  PrimaryButton 56dp
│                                    │
│  ¿No tienes cuenta?                │
│  [        Crear cuenta         ]   │  SecondaryButton
│                                    │
└────────────────────────────────────┘
```
Contenido centrado verticalmente en `Column(verticalScroll)`, ancho máx. 480 dp, márgenes 24 dp, `imePadding()`.

## Wireframe - Registro
```text
┌────────────────────────────────────┐
│ (Volver)  Crear cuenta             │
│  Tu nombre *        [ (Usuario)  ] │  RegisterRequest.nombre
│  Nombre de la finca*[ (Finca)    ] │  RegisterRequest.fincaNombre
│  Correo *           [ (Correo)   ] │  RegisterRequest.email
│  Contraseña *       [ (Clave)    ] │  RegisterRequest.password, helper "Mínimo 8 caracteres"
│  [       Crear cuenta          ]   │
└────────────────────────────────────┘
```

## Datos
- `LoginRequest(email, password)` -> `ApiService.login` -> `TokenResponse(accessToken, refreshToken, tokenType)`.
- `RegisterRequest(email, password, nombre, fincaNombre)` -> `ApiService.register`.
- `TokenStore.saveTokens(access, refresh)`; luego `SyncManager.syncAll()` (como hoy).

```kotlin
data class LoginUiState(
    val email: String = "", val password: String = "", val passwordVisible: Boolean = false,
    val emailError: UiText? = null, val passwordError: UiText? = null,
    val generalError: UiText? = null, val isSubmitting: Boolean = false,
)
```

## Estados e interacciones
| Situación | Comportamiento |
|---|---|
| Campos vacíos al tocar Ingresar | Error bajo cada campo vacío ("Escribe tu correo", "Escribe tu contraseña"); foco al primero |
| Correo con formato inválido | "Revisa el correo" (validación `Patterns.EMAIL_ADDRESS`) |
| Enviando | Botón con spinner, campos deshabilitados |
| Sin internet (`ConnectivityObserver`) | Tarjeta `offlineContainer`: "Necesitas internet para ingresar la primera vez" y botón deshabilitado |
| HTTP 401 | "Correo o contraseña incorrectos" |
| HTTP 429 (throttler 10/min) | "Demasiados intentos. Espera un minuto." |
| Otro error / timeout | "No pudimos conectar con el servidor. Intenta de nuevo." |
| Registro 409/400 | "Ese correo ya está registrado" / mensajes de validación simples |
| Éxito | Navegar a `Graph.MAIN` limpiando `auth` del back stack |

No mostrar `exception.message`. No registrar contraseñas en logs.

## Criterios de aceptación
- [ ] Usuario demo `demo@agrocontrol.local` / `demo1234` entra y llega a Inicio.
- [ ] Si ya hay token al abrir la app, Login no se muestra.
- [ ] Teclado de correo con `@`; contraseña oculta con botón Ver/Ocultar (48 dp, cd "Mostrar contraseña").
- [ ] Rotar la pantalla no borra lo escrito (la contraseña puede no persistir).
- [ ] Registro usa los cuatro campos reales; el botón demo aleatorio desaparece.
- [ ] Sin emojis ni Toasts.

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

Frame 2 "Iniciar sesión". El registro se movió a su propia ficha: [14-registro.md](14-registro.md) (ignorar la sección "Wireframe - Registro" de arriba).

```text
│  Correo electrónico *  [ (Correo) ...          ] │ [Existe] LoginRequest.email
│  Contraseña *          [ (Clave) ****    (Ver) ] │ [Existe] LoginRequest.password
│  [x] Recordar mi correo      ¿Olvidaste tu contraseña? │
│  [            Ingresar                  ]        │
│  ¿No tienes cuenta?  Crear cuenta (TextButton 48dp) │
```

| Control Figma | Decisión | Datos |
|---|---|---|
| Recordar usuario | `Checkbox` 48 dp "Recordar mi correo". Guarda **solo el correo** en `SharedPreferences agrocontrol_ui_prefs/remembered_email`; nunca la contraseña (la sesión ya persiste con `TokenStore`) | [UI] |
| ¿Olvidaste tu clave? | `TextButton` "¿Olvidaste tu contraseña?". v1: `InfoDialog` "Pide al administrador de la finca que restablezca tu contraseña." v2: pantalla de correo -> `POST /auth/forgot-password` (flag `RECUPERAR_CLAVE`) | [Backend] |
| Registrarse (link) | Navega a `registro` | [Existe] |
| Ojo de contraseña | `IconButton` Ver/Ocultar 48 dp | [UI] |

Criterios adicionales:
- [ ] Con "Recordar mi correo" marcado, el correo aparece prellenado tras cerrar sesión.
- [ ] Texto del botón "Ingresar" en mayúscula inicial, no "INGRESAR".
