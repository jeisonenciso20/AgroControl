# Pantalla: Producción

Ruta: `produccion` (tab 3). Reemplaza `ProduccionActivity`. Paquete `ui/feature/produccion`.

## Propósito
Ver los registros de producción (leche, carne u otros rubros) por fecha y los totales del periodo.

## Wireframe
```text
┌────────────────────────────────────┐
│ Producción                         │
│ [7 días][30 días][Todo]            │  ChoiceChipGroup periodo
│ [Todos][Leche][Carne][...]         │  FilterChipsRow por productType
├────────────────────────────────────┤
│ ┌──────────────┐ ┌──────────────┐  │
│ │ (Leche)      │ │ (Peso)       │  │  KpiTile por rubro (top 2-4)
│ │ 1.540 litros │ │ 320 kg       │  │
│ │ Leche        │ │ Carne        │  │
│ └──────────────┘ └──────────────┘  │
│ Hoy                                │  encabezado de grupo por fecha (sticky)
│ (Leche) Leche        210 litros    │  AgroListItem
│         Ordeño mañana              │  notas
│ Ayer                               │
│ (Leche) Leche        198 litros    │
│ 12/10/2026                         │
│ ...                                │
└────────────────────────────────────┘
```

## Datos (`ProductionLocal`)
- `productType` -> título y filtro; icono: contiene "leche" -> `Leche` (color `milk`), "carne" -> `Peso` (color `meat`), otro -> `Produccion`.
- `cantidad` + `unidad` -> "210 litros" (formatNumero + unidad tal cual).
- `fecha` (yyyy-MM-dd) -> grupos "Hoy", "Ayer", o dd/MM/yyyy. Orden: `allProduction()` ya viene `fecha DESC`.
- `notas` -> línea secundaria (ocultar si vacía).
- KPI: suma de `cantidad` por `productType` **y** `unidad` dentro del periodo (no sumar litros con kg).

## Alta de registros
En v1 la producción no se crea en la app (solo pull; el outbox actual solo encola ANIMAL). **No** mostrar FAB de alta. Mostrar en el encabezado `LastSyncText`. Dejar documentado: cuando exista `ProductionRepository.insert` con outbox `PRODUCTION`, añadir FAB "Registrar producción" con formulario (rubro chips, cantidad NumberField, unidad, fecha, notas).

## Estados
- Vacío: `EmptyState(Produccion, "Sin registros de producción", "Los registros aparecen al sincronizar con el servidor.", "Sincronizar ahora")`.
- Vacío por filtro: "No hay registros en este periodo" + "Ver todo".

## Criterios de aceptación
- [ ] Totales por rubro correctos y separados por unidad.
- [ ] Agrupación por día legible; cifras con formato colombiano.
- [ ] Funciona sin internet con la caché.

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

Frame 13 "Producción de leche". La pantalla del tab Producción se reorganiza alrededor del **ordeño diario**, manteniendo abajo el listado general por rubro.

```text
┌────────────────────────────────────┐
│ Producción de leche   (Fecha) Hoy v│  selector de día (DateField compacto)
│ ┌──────────┐┌──────────┐┌─────────┐│
│ │ 420 L    ││ 18       ││ 23,3 L  ││  KpiTile x3: total del día, animales ordeñados, promedio L/animal
│ │ Total hoy││ Animales ││ Prom/vaca│
│ └──────────┘└──────────┘└─────────┘│
│ [ (Buscar) Buscar vaca          ]  │
│ ┌────────────────────────────────┐ │
│ │ (B) Lucero · 001               │ │  MilkingCard (03 §11)
│ │ Mañana 12 L  Tarde 10 L  Total 22 L│
│ │ Ver historial >                │ │  -> animales/{codigo}/produccion
│ └────────────────────────────────┘ │
│ Otros rubros (carne, etc.)       > │  -> lista general (diseño anterior de esta ficha)
│           [+ Registrar producción] │  AgroFab (flag REGISTRAR_PRODUCCION)
└────────────────────────────────────┘
```

| Elemento Figma | Datos |
|---|---|
| Total hoy (L) | Suma de `ProductionLocal.cantidad` con `productType` leche y `fecha` = día seleccionado [Existe] |
| N.º animales | Animales distintos con registro ese día [Backend] (`animalId/animalCodigo`) |
| Promedio L/animal | total / n.º animales [Backend] |
| Tarjeta por vaca mañana/tarde/total | [Backend] (`animalCodigo`, `jornada`) |
| + Registrar producción | Ficha [18-produccion-registrar.md](18-produccion-registrar.md) [Backend] |

Mientras no existan `animalCodigo` y `jornada`: mostrar solo "Total hoy" y el listado por registro (diseño original de esta ficha); KPI de animales y promedio en estado `Unavailable` con caption "Disponible al registrar por vaca".
