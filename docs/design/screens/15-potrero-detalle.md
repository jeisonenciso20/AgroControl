# Pantalla: Detalle de potrero

Ruta: `potreros/{serverId}`. Figma frame 7. Paquete `ui/feature/potreros/detalle`.

Leyenda: [Existe] · [UI] · [Backend].

## Wireframe
```text
┌────────────────────────────────────┐
│ (Volver) Potrero 3          (Editar)│  Editar con flag CREAR_POTRERO
├────────────────────────────────────┤
│ ┌────────────────────────────────┐ │
│ │ (Potrero 48dp) Potrero 3       │ │  headlineSmall
│ │ [Disponible]                   │ │  StatusChip estado
│ └────────────────────────────────┘ │
│ ┌────────┐┌───────────┐┌─────────┐ │
│ │ 5,2 ha ││ Pasto     ││ 12      │ │  mini KPI: área, estado del pasto, animales actuales
│ │ Área   ││ Bueno     ││ Animales│ │
│ └────────┘└───────────┘└─────────┘ │
│ Datos                              │
│ (Pasto) Tipo de pasto   Brachiaria │  StatRow
│ (Fecha) Última ocupación 20/09/2026│
│ (Fecha) Próximo uso      30/10/2026│
│ (Nota)  Observación      ...       │
│ Trabajos pendientes        Ver todos│
│ (Fumigar) Fumigar     03/10 [Vencido]│ TaskListItem filtrado
│ (Abonar)  Abonar      05/10        │
│ Animales en este potrero   Ver todos│
│ (B) Lucero · Vaca · 480 kg      >  │  AnimalListItem compacto, máx. 5
│ [     (Agregar) Registrar trabajo ]│  PrimaryButton 56dp fijo abajo (flag REGISTRAR_TRABAJO)
└────────────────────────────────────┘
```

## Datos
| Elemento | Fuente | Origen |
|---|---|---|
| Nombre, área, estado | `PotreroLocal.nombre`, `areaHectareas`, `estado` | [Existe] |
| Especies permitidas | `especiesJson` parseado | [Existe] |
| Estado del pasto (Bueno/Regular/Malo) | campo nuevo `estadoPasto` | [Backend] |
| Tipo de pasto, observación, fecha próximo uso | campos nuevos (ver ficha 16) | [Backend] |
| Última ocupación | `fechaUltimaOcupacion` o historial `AnimalPotrero` (existe en el esquema del servidor según `docs/architecture/database.md`) | [Backend] |
| Animales actuales | `animalRepository.getAll().filter { it.potrero.equals(nombre, true) }` | [Existe] |
| Trabajos pendientes | `LocalCacheDao.pendingTasks().filter { it.referencia.equals(nombre, true) }` | [Existe] (convención de `referencia`) |

Elementos [Backend] sin datos: ocultar la fila (no mostrar "-" masivo). El mini KPI de pasto se reemplaza por "Especies" mientras no exista.

## Interacciones
- Registrar trabajo -> `potreros/trabajo?potrero={nombre}`.
- Animal -> Detalle de animal. "Ver todos" animales -> lista Animales con búsqueda prellenada por potrero (filtro en memoria por `potrero`).
- Recargar en `ON_RESUME`.

## Estados
- `serverId` no encontrado: `ErrorState` "No encontramos este potrero" + "Volver".
- Sin trabajos: línea "Sin trabajos pendientes" con icono `Exito`.

## Criterios de aceptación
- [ ] Cuenta de animales coincide con los animales cuyo `potrero` es este nombre.
- [ ] Trabajos vencidos marcados con icono + texto + color.
- [ ] Ningún campo [Backend] muestra datos inventados.
