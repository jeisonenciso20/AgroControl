# Pantalla: Potreros

Ruta: `potreros` (desde Inicio y Más). Reemplaza `PotrerosActivity`. Paquete `ui/feature/potreros`.

## Propósito
Consultar los potreros de la finca, su área, estado y qué especies admite. Solo lectura (los potreros llegan por pull del servidor; no hay alta local en v1).

## Wireframe
```text
┌────────────────────────────────────┐
│ (Volver) Potreros                  │
│ 8 potreros · 42,5 ha en total      │  resumen bodyLarge
│ Actualizado hace 5 min             │  LastSyncText
├────────────────────────────────────┤
│ [Todos][Disponible][En uso][Descanso]│ FilterChipsRow (valores reales de estado)
│ ┌────────────────────────────────┐ │
│ │ (Potrero) Potrero La Ceiba     │ │  titleMedium
│ │ (Area) 5,2 ha      [Disponible]│ │  StatusChip
│ │ Admite: Vaca, Ternera          │ │  bodyMedium, chips pequeños
│ └────────────────────────────────┘ │
└────────────────────────────────────┘
```

## Datos (`PotreroLocal`)
- `nombre` -> título.
- `areaHectareas: Double?` -> "5,2 ha" o "Área sin registrar".
- `estado: String` -> `StatusChip`. Mapeo de tono por texto (ignore case): contiene "dispon"/"libre" -> SUCCESS; "uso"/"ocup" -> INFO; "descan"/"recup" -> WARNING; otro -> NEUTRAL. Mostrar el texto tal cual viene, con mayúscula inicial.
- `especiesJson: String` -> parsear con Gson a `List<String>` en el ViewModel (hoy se muestra el JSON crudo); si falla, ocultar la línea.
- Filtros de estado: generados dinámicamente desde los valores distintos de `estado`.
- Opcional: contar animales por potrero con `animalRepository.getAll().groupBy { it.potrero }` y mostrar "12 animales" (coincidencia por nombre).

## Estados
- Vacío: `EmptyState(Potrero, "No hay potreros guardados", "Conéctate a internet y sincroniza para descargarlos.", "Sincronizar ahora")` -> `syncNow()`.
- Cargando / Error: estándar de 03 §7.

## Criterios de aceptación
- [ ] No se muestra JSON crudo.
- [ ] Funciona offline con la caché.
- [ ] Botón Sincronizar del estado vacío actualiza la lista al terminar.

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

Frame 6 "Potreros". Potreros pasa a ser **tab 4** de la barra inferior (sin botón Volver).

```text
┌────────────────────────────────────┐
│ Potreros                           │
│ [ (Buscar) Buscar potrero       ]  │
│ [Todos][Disponible][En recuperación][No disponible]│
│ ┌────────────────────────────────┐ │
│ │ (Potrero) Potrero 3   [Disponible]│ │
│ │ (Area) 5,2 ha                  │ │
│ │ (Fecha) Próximo: Fumigar · 03/10│ │  próximo trabajo
│ └────────────────────────────────┘ │
│                [+ Agregar potrero] │  AgroFab (flag CREAR_POTRERO)
│ Inicio Animales Producción Potr. Más│
└────────────────────────────────────┘
```

| Elemento Figma | Decisión | Datos |
|---|---|---|
| Búsqueda | Filtro en memoria por `nombre` | [Existe] |
| Estado Disponible / Recuperación / No disponible | `StatusChip`: Disponible -> SUCCESS, En recuperación -> WARNING, No disponible -> ERROR (con icono). Mapeo desde `PotreroLocal.estado` por texto; valores desconocidos -> NEUTRAL | [Existe] (texto libre del servidor; acordar enum `DISPONIBLE/RECUPERACION/NO_DISPONIBLE` con backend) |
| Hectáreas | `areaHectareas` | [Existe] |
| Próximo trabajo + fecha | Primer `PendingTaskLocal` con `referencia` == `nombre` del potrero (ignore case), ordenado por `vencimiento` | [Existe] (convención de `referencia`; confirmar con backend) |
| + Agregar potrero | FAB -> `potreros/nuevo` | [Backend] |
| Tocar tarjeta | -> `potreros/{serverId}` (Detalle, ficha 15) | [Existe] |
