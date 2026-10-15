# Pantalla: Pendientes (tareas)

Ruta: `pendientes` (ya no es tab: se abre desde Inicio > "Ver todos", la campana del header de Inicio y Más; ver 04 §2). Reemplaza `PendientesActivity`. Paquete `ui/feature/pendientes`.

## Propósito
Lista de trabajo del día: qué está vencido, qué vence hoy y qué viene.

## Wireframe
```text
┌────────────────────────────────────┐
│ Pendientes                     12  │
│ [Todas][Vencidas][Hoy][Próximas]   │  FilterChipsRow con contadores
├────────────────────────────────────┤
│ Vencidas (2)                       │  encabezado sticky, color error
│ ┌────────────────────────────────┐ │
│ │ (Vencido) Vacunar lote 2       │ │  TaskListItem
│ │ Vacunación · Venció el 12/10   │ │  tipo · vencimiento
│ │ Ref: Potrero 3                 │ │  referencia
│ └────────────────────────────────┘ │
│ Hoy (1)                            │
│ (Reloj) Pesaje de terneros         │
│ Próximas (9)                       │
│ ...                                │
├────────────────────────────────────┤
│ Inicio Animales Producción Potr. Más│
└────────────────────────────────────┘
```

## Datos (`PendingTaskLocal`)
- Fuente: `LocalCacheDao.pendingTasks()` (ya filtra `status = 'PENDING'` y ordena por `vencimiento`).
- `titulo` -> título; `tipo` -> etiqueta; `vencimiento` (yyyy-MM-dd) -> urgencia (03 §4 `TaskListItem`) y texto ("Venció el 12/10", "Vence hoy", "Vence mañana", "Vence el 20/10"); `referencia` -> "Ref: ..." (ocultar si vacía).
- Refrescar con red: `syncNow()` (el pull trae `pendingTasks`). Alternativamente `api.tasks()` solo para mostrar, sin escribir en Room desde la UI.
- Badge de la campana en Inicio = vencidas + hoy.

## Acciones
- **Marcar terminado:** `Checkbox` al inicio de cada `TaskListItem` (área táctil ≥ 48 dp) en Pendientes, Inicio (“Pendientes de hoy”) y detalle de potrero.
- Local: `status = COMPLETED` (UI “Terminado”); deja de aparecer en `pendingTasks()` (`PENDING` only).
- Sync offline-first: outbox `PENDING_TASK` / `UPDATE` con payload `{ status: COMPLETED }` → backend `tasks.complete` vía `sync/push`. Tras crear offline (`local-*`) y completar antes del push, el UPDATE se encola al recibir el `serverId`.
- Tap en el resto del ítem abre detalle (`TaskDetailDialog`: ref, vencimiento, observaciones).

## Estados
- Vacío: `EmptyState(Exito, "Todo al día", "No tienes tareas pendientes.")` en `successContainer`.
- Pull-to-refresh + botón de sincronizar en el estado vacío.

## Criterios de aceptación
- [ ] Agrupación Vencidas / Hoy / Próximas correcta según fecha del teléfono.
- [ ] Urgencia comunicada con icono + texto + color.
- [ ] Badge de la campana coincide con vencidas + hoy.

## Actualización Figma (prevalece donde difiera)
- Los ítems del Figma ("Potrero 3 - Fumigación 03/10", "Vaca 024 - Inseminación programada") se representan con `TaskListItem`: `titulo` [Existe], `tipo` [Existe] con icono por tipo (Fumigar/Abonar/Limpiar/Revisar pasto -> `Potrero`; inseminación/celo/parto -> `Reproduccion`; otro -> `Pendientes`), `referencia` [Existe] como enlace: si coincide con un `PotreroLocal.nombre` abre Detalle de potrero; si coincide con un `Animal.codigo` abre Detalle de animal.
- Barra inferior: Pendientes no muestra barra seleccionada; el tab activo queda en el origen (Inicio o Más).
- Acción "Registrar trabajo" en la TopAppBar -> `potreros/trabajo` (flag `REGISTRAR_TRABAJO`, [Backend]).
