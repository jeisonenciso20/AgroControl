# Pantalla: Crear cuenta

Ruta: `registro` (grafo `auth`). Figma frame 3. Paquete `ui/feature/registro`. Reemplaza la sección "Wireframe - Registro" de `01-login.md` y el botón de registro demo de `LoginActivity`.

Leyenda: [Existe] · [UI] · [Backend].

## Wireframe
```text
┌────────────────────────────────────┐
│ (Volver) Crear cuenta              │  TopAppBar con Volver (el Figma no lo tiene: corregido)
├────────────────────────────────────┤
│ Crea tu finca en AgroControl       │  bodyLarge
│ Tu nombre *                        │
│ [ (Usuario) Juan Pérez          ]  │  KeyboardCapitalization.Words
│ Nombre de la finca *               │
│ [ (Finca) La Esperanza          ]  │
│ Correo electrónico *               │
│ [ (Correo) tu@correo.com        ]  │
│ Contraseña *                       │
│ [ (Clave) ********       (Ver)  ]  │  helper "Mínimo 8 caracteres"
│ Confirmar contraseña *             │
│ [ (Clave) ********       (Ver)  ]  │
│                                    │
│ [         Crear cuenta         ]   │  PrimaryButton 56dp (Figma: REGISTRARME)
│ ¿Ya tienes cuenta? Ingresar        │  TextButton -> login
└────────────────────────────────────┘
```

## Campos
| Campo | Binding | Validación | Origen |
|---|---|---|---|
| Tu nombre | `RegisterRequest.nombre` | Obligatorio, >= 2 caracteres | [Existe] |
| Nombre de la finca | `RegisterRequest.fincaNombre` | Obligatorio. **No está en el Figma** pero la API lo exige (el registro crea `Finca` + `User`) | [Existe] |
| Correo | `RegisterRequest.email` | Obligatorio, formato email | [Existe] |
| Contraseña | `RegisterRequest.password` | Obligatorio, >= 8 caracteres (confirmar regla con backend) | [Existe] |
| Confirmar contraseña | solo cliente | Igual a contraseña: "Las contraseñas no coinciden" | [UI] |

Tras éxito: `TokenStore.saveTokens` [Existe], guardar `nombre` y `fincaNombre` en `agrocontrol_ui_prefs` [UI] (para el saludo "Hola, {nombre}" y la cabecera de Más), `syncAll()` y navegar a `Graph.MAIN` limpiando `auth`.

## Estados
- Sin internet: tarjeta "Necesitas internet para crear la cuenta", botón deshabilitado.
- 409 / correo existente: error en campo Correo "Este correo ya tiene cuenta" + acción "Ingresar".
- 400: mensajes de validación por campo; 429: "Demasiados intentos. Espera un minuto."
- Enviando: botón con spinner, campos deshabilitados.
- Atrás con datos escritos: `ConfirmDialog` "¿Descartar el registro?".

## Criterios de aceptación
- [ ] Botón Volver y Atrás del sistema regresan a Login.
- [ ] Ya no se crean cuentas demo aleatorias.
- [ ] Todos los errores se muestran en línea; foco al primer campo con error.
