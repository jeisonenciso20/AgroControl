# Pantalla: Más (perfil, sincronización y ajustes)

Ruta: `mas` (tab 5). Reemplaza `MasActivity`. Paquete `ui/feature/mas`.

## Propósito
Acceso a módulos secundarios, control explícito de la sincronización, apariencia y cierre de sesión.

## Wireframe
```text
┌────────────────────────────────────┐
│ Más                                │
├────────────────────────────────────┤
│ Sincronización                     │  SectionHeader
│ ┌────────────────────────────────┐ │
│ │ (CloudDone) Todo enviado       │ │  estado (o "3 cambios por enviar", "Sin internet")
│ │ Actualizado hace 5 min         │ │  LastSyncText
│ │ [   (Sync) Sincronizar ahora  ]│ │  PrimaryButton 56dp, loading durante sync
│ └────────────────────────────────┘ │
│ Módulos                            │
│ (Pendientes) Pendientes   [3]  >   │  AgroListItem 72dp, badge
│ (Reprod)   Reproducción        >   │
│ (Finanzas) Finanzas            >   │
│ (Reportes) Reportes            >   │
│ Apariencia                         │
│ (Tema) Tema   [Sistema][Claro][Oscuro]│ ChoiceChipGroup
│        Para trabajar al sol usa Claro │ bodySmall
│ Cuenta                             │
│ (Info) Versión 1.0                 │  BuildConfig/PackageInfo versionName
│ (Salir) Cerrar sesión              │  texto en color error
└────────────────────────────────────┘
```

## Datos
- `SyncStatusRepository.status`: `isOnline`, `pendingCount`, `lastPullIso`, `isSyncing`, `lastError`.
- Sincronizar ahora -> `syncNow()` = `SyncManager.syncAll()` existente (misma lógica que `MasActivity.syncNow()`).
- Tema -> `SharedPreferences agrocontrol_ui_prefs/theme_mode` (02 §4).
- Cerrar sesión -> `TokenStore.clear()` (igual que `cerrarSesion()`), luego navegar a `Graph.AUTH`.
- No hay datos de perfil (nombre/finca) en la app: no mostrar perfil inventado. Si en el futuro se guarda, añadir cabecera con `Usuario` y `Finca`.

## Estados de la tarjeta de sincronización
| Condición | Icono / texto | Botón |
|---|---|---|
| Sin internet | `SinInternet` "Sin internet. {n} cambios esperando" | deshabilitado, texto "Esperando internet" |
| Sincronizando | `Sincronizar` "Enviando cambios..." | loading |
| Pendientes | `PorEnviar` "{n} cambios por enviar" | "Sincronizar ahora" |
| Error | `Error` "No se pudo sincronizar" | "Reintentar" |
| Al día | `Sincronizado` "Todo enviado" | "Sincronizar ahora" |
Resultado: Snackbar "Sincronización completa" / "No se pudo sincronizar. Intenta más tarde." (reemplaza "Sync falló").

## Cerrar sesión
`ConfirmDialog("Cerrar sesión", "¿Seguro que quieres salir?", "Cerrar sesión", destructive = true)`. Si `pendingCount > 0`: mensaje "Tienes {n} cambios sin enviar. Sincroniza antes de salir para no perderlos." con botones "Sincronizar primero" (primario) y "Salir de todos modos" (destructivo, texto).

## Criterios de aceptación
- [ ] "Sincronizar ahora" funciona y refleja estados en tiempo real.
- [ ] Cerrar sesión ahora es accesible (hoy `cerrarSesion()` existe pero ningún botón lo llama).
- [ ] Cambiar tema se aplica al instante y persiste al reiniciar.
- [ ] Todos los ítems >= 72 dp.

## Actualización Figma (prevalece donde difiera)
- Módulos en Más: Pendientes, Reproducción, Finanzas, Reportes (Potreros pasó a la barra inferior; Finanzas sale de la barra, ver 04 §2).
- El icono de perfil del header de Inicio navega a Más y hace scroll a la sección Cuenta.
- Cabecera de perfil opcional (nombre y finca) solo si se guardan localmente tras login/registro: [Backend] el `TokenResponse` no trae perfil; requiere `GET /auth/me` o guardar `nombre`/`fincaNombre` del registro en preferencias [UI]. No inventar datos.

