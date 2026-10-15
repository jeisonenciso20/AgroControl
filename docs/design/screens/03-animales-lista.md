# Pantalla: Lista de animales

Ruta: `animales` (tab 2). Reemplaza `AnimalesActivity` + `activity_animales.xml` + `item_animal_card.xml`. Paquete `ui/feature/animales/lista`.

## Propósito
Encontrar un animal rápido (por código de arete, nombre o tipo) y registrar animales nuevos.

## Wireframe
```text
┌────────────────────────────────────┐
│ Animales                     124   │  titleLarge + contador total
├────────────────────────────────────┤
│ [ (Buscar) Código, nombre o tipo (X)]│ SearchBar/AgroTextField 56dp
│ [Todos][Vaca][Toro][Ternero][Ternera]│ FilterChipsRow
│ [Banner sync]                      │
├────────────────────────────────────┤
│ ┌────────────────────────────────┐ │
│ │(B) Lucero              [Hembra]│ │  AnimalListItem
│ │    Vaca · Holstein             │ │
│ │    #001 · 480 kg · Potrero 1   │ │
│ └────────────────────────────────┘ │
│ ┌────────────────────────────────┐ │
│ │(B) Toro 014            [Macho] │ │
│ │    Toro · Brahman  [Por enviar]│ │
│ │    #014 · 620 kg · Potrero 3   │ │
│ └────────────────────────────────┘ │
│                 [+ Agregar animal] │  AgroFab
├────────────────────────────────────┤
│ Inicio Animales Producción Potr. Más│
└────────────────────────────────────┘
```

## Datos
- `AnimalRepository.search(query)` (busca en `codigo`, `nombre`, `tipo`); query vacía = todos, orden por `codigo`.
- Filtro de tipo: en memoria sobre el resultado (`Animal.tipo` en {Vaca, Toro, Ternero, Ternera}), sin cambiar el DAO.
- Ítem: `AnimalItemUi` (ver 02 §7): `titulo = nombre.ifBlank { "$tipo $codigo" }`, `tipo`, `raza`, `sexo`, `peso`, `potrero`, `syncState`.

## Estados
| Estado | UI |
|---|---|
| Cargando primera vez | 5 `SkeletonListItem` |
| Lista vacía sin búsqueda | `EmptyState(Bovino, "Aún no hay animales", "Registra tu primer animal para empezar.", "Agregar animal")` |
| Búsqueda sin resultados | `EmptyState(Buscar, "Sin resultados para \"{q}\"", "Revisa el código o borra la búsqueda.", "Borrar búsqueda")` |
| Error Room | `ErrorState` con Reintentar |

## Interacciones
- Escribir: debounce 250 ms. Botón X limpia. `ImeAction.Search` oculta teclado.
- Tocar ítem -> `animales/{codigo}`.
- FAB -> `animales/nuevo`. Al volver con guardado OK, Snackbar y recarga.
- Recargar en `ON_RESUME` (como hoy `onResume`).
- Lista con `LazyColumn` y `key = codigo`; padding inferior para no quedar bajo el FAB.

## Criterios de aceptación
- [ ] Buscar "001" encuentra el animal semilla.
- [ ] Un animal creado sin red aparece con chip "Por enviar"; tras sincronizar cambia a sin chip.
- [ ] Ítems >= 88 dp, toda la tarjeta clicable, TalkBack lee "Lucero, Vaca Holstein, hembra, código 001, 480 kilos, Potrero 1".
- [ ] FAB con texto visible y sin tapar el último ítem.

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

Frame 5 "Animales".

| Elemento Figma | Decisión | Datos |
|---|---|---|
| Búsqueda | Igual (código, nombre, tipo) | [Existe] `search` |
| Chips Todos/Vacas/Toros/Terneros | `FilterChipsRow`: Todos, Vacas (`tipo`=Vaca), Toros (Toro), Terneros (Ternero **y** Ternera). Con contador "Vacas (30)" | [Existe] |
| "+ Agregar animal" | `AgroFab` extendido "Agregar animal" (no botón en la cabecera) | [Existe] |
| Tarjeta con foto | `AnimalListItem` con `AnimalAvatar` (03 §11): foto si existe, si no círculo con `Bovino` + iniciales del código | Foto [Backend] (flag `FOTOS_ANIMAL`) |
| nombre, raza + sexo, peso, potrero | Título, "Holstein · Hembra" + `SexChip`, "480 kg", "Potrero 1" con icono `Potrero` | [Existe] |

Coherencia de códigos: mostrar `codigo` exactamente como está guardado ("001" no se convierte en "01").
