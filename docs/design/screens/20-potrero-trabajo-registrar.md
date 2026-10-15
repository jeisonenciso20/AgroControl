# Pantalla: Registrar trabajo de potrero

Ruta: `potreros/trabajo?potrero={nombre}`. Figma frame 15. Paquete `ui/feature/potreros/trabajo`. Sin barra inferior. **Flag `REGISTRAR_TRABAJO` [Backend]**.

Leyenda: [Existe] · [UI] · [Backend].

## Wireframe
```text
┌────────────────────────────────────┐
│ (X) Registrar trabajo              │
├────────────────────────────────────┤
│ Potrero *                          │
│ [ (Potrero) Potrero 3           v ]│  AgroDropdownField (caché de potreros)
│ Tipo de trabajo *                  │
│ [(Fumigar) Fumigar] [(Abonar) Abonar]│ WorkTypeChips (03 §11), FlowRow, 56dp
│ [(Limpiar) Limpiar] [(Pasto) Revisar pasto]│
│ [(Otro) Otro]                      │
│ ¿Cuál? *  [                     ]  │  solo si Otro
│ Fecha programada *                 │
│ [ (Fecha) 20/10/2026            ]  │  DateField, por defecto hoy; permite futuras
│ Observación                        │
│ [ p. ej. producto y dosis       ]  │
├────────────────────────────────────┤
│ [        Guardar trabajo         ] │
└────────────────────────────────────┘
```

## Modelado (reutiliza Pendientes)
El trabajo se guarda como **tarea pendiente**, así aparece automáticamente en Detalle de potrero, Lista de potreros (próximo trabajo), Pendientes e Inicio:

| Campo UI | `PendingTaskLocal` / DTO | Origen |
|---|---|---|
| Tipo de trabajo | `tipo` = FUMIGAR / ABONAR / LIMPIAR / REVISAR_PASTO / OTRO | [Existe] campo |
| Título (derivado) | `titulo` = "{Tipo} {Potrero}" o texto de "¿Cuál?" | [Existe] |
| Fecha programada | `vencimiento` yyyy-MM-dd | [Existe] |
| Potrero | `referencia` = nombre del potrero | [Existe] (convención; ideal `potreroId` [Backend]) |
| Estado | `status` = "PENDING" | [Existe] |
| Observación | campo nuevo `observacion` | [Backend] |

Persistencia requerida: `PendingTaskRepository.insert` (Room + `SyncOutbox(entity = "TASK", operation = "CREATE")` + `schedule()`), PK local temporal; backend aceptando `TASK` en `/sync/push`.

## Interacciones
- Potrero preseleccionado si llega el argumento (desde Detalle de potrero).
- Guardar -> volver -> Snackbar "Trabajo programado para el 20/10".

## Criterios de aceptación
- [ ] Tipos como chips grandes con icono y texto (no lista desplegable).
- [ ] El trabajo aparece en el detalle del potrero y en Pendientes tras guardar (offline).
- [ ] "Otro" exige descripción.
