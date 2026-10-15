# Pantalla: Detalle de animal

Ruta: `animales/{codigo}`. Reemplaza `DetalleAnimalActivity` + `activity_detalle_animal.xml`. Paquete `ui/feature/animales/detalle`.

## Propósito
Ver la ficha completa de un animal y acceder a acciones sobre él.

## Wireframe
```text
┌────────────────────────────────────┐
│ (Volver) Detalle         (Editar)  │
├────────────────────────────────────┤
│ ┌────────────────────────────────┐ │
│ │ (B 64dp)  Lucero               │ │  headlineSmall
│ │           Vaca · Holstein      │ │  bodyLarge
│ │           [Hembra] [Enviado]   │ │  SexChip + SyncStateChip
│ └────────────────────────────────┘ │
│ ┌──────────┐┌──────────┐┌────────┐ │
│ │ 480 kg   ││ 6 años   ││ Pot. 1 │ │  3 mini KPI: peso, edad, potrero
│ │ Peso     ││ Edad     ││Potrero │ │
│ └──────────┘└──────────┘└────────┘ │
│ Datos                              │
│ (Codigo) Código        001         │  StatRow
│ (Raza)   Raza          Holstein    │
│ (Fecha)  Nacimiento    15/03/2020  │
│ (Peso)   Peso          480 kg      │
│ (Potrero)Potrero       Potrero 1   │
│ Observaciones                      │
│ ┌────────────────────────────────┐ │
│ │ Sin observaciones              │ │
│ └────────────────────────────────┘ │
│ Registros                          │
│ [ (Leche) Producción          > ]  │  AgroListItem -> tab Producción
│ [ (Reprod) Reproducción       > ]  │  -> Reproducción
│ [        Editar animal         ]   │  SecondaryButton (paso 12)
└────────────────────────────────────┘
```

## Datos
Cargar por `codigo` con `AnimalDao.findByCodigo(codigo)` (existente). Campos de `Animal`: `codigo`, `nombre`, `tipo`, `raza`, `sexo`, `peso`, `fechaNacimiento` (yyyy-MM-dd -> dd/MM/yyyy y edad calculada), `potrero`, `observacion` (vacío -> "Sin observaciones"), `syncState`, `serverId` (no mostrar). Título = `nombre.ifBlank { "$tipo $codigo" }`.

```kotlin
sealed interface AnimalDetalleUiState {
    data object Loading : AnimalDetalleUiState
    data object NotFound : AnimalDetalleUiState
    data class Content(val animal: AnimalDetalleUi) : AnimalDetalleUiState
}
```

## Estados
- `NotFound`: `ErrorState("No encontramos este animal", "Puede que se haya eliminado.", "Volver a la lista")`. **No** inventar valores por defecto como hace hoy (`"Holstein"`, `450.0`).
- Fecha inválida: mostrar el texto crudo y ocultar edad.

## Interacciones
- Editar (icono en barra y botón) -> `animales/{codigo}/editar`. Mientras `FeatureFlags.EDITAR_ANIMAL = false`, ocultar ambos (no mostrar "Próximamente").
- Producción / Reproducción: navegan al módulo (aún sin filtrar por animal, porque `ProductionLocal` no tiene animalId). Texto auxiliar: "Registros generales de la finca".
- Recargar en `ON_RESUME` (para reflejar edición).

## Criterios de aceptación
- [ ] Abrir desde la lista muestra exactamente los datos de Room del código elegido.
- [ ] Edad correcta ("6 años 7 meses") y fecha en dd/MM/yyyy.
- [ ] Ningún botón muestra "Próximamente" ni Toast.
- [ ] Todos los StatRow legibles con fuente al 200%.

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

Frame 8 "Detalle animal". Reemplaza el layout de una sola columna por cabecera + 3 pestañas.

```text
┌────────────────────────────────────┐
│ (Volver) Lucero               (Editar)│
│ ┌────────────────────────────────┐ │
│ │      [ Foto 16:9 o avatar ]    │ │  AnimalAvatar grande (alto 200dp)
│ └────────────────────────────────┘ │
│ Lucero                  [Hembra]   │  headlineSmall + SexChip
│ ┌───────┐┌───────┐┌──────┐┌───────┐│  4 mini-KPI en FlowRow (2x2 en pantallas angostas)
│ │Holstein││480 kg ││6 años││Potr. 1││  raza / peso / edad / potrero actual (tocable -> Detalle potrero)
│ └───────┘└───────┘└──────┘└───────┘│
│ [Información][Producción][Reproducción]│ PrimaryTabRow, tabs 48dp
├────────────────────────────────────┤
│ (contenido del tab)                │
└────────────────────────────────────┘
```

| Tab | Contenido | Datos |
|---|---|---|
| Información | StatRow: Código, Tipo, Fecha de nacimiento, Estado, Observación; botón "Editar animal" | [Existe] salvo "Estado" |
| Producción | Resumen corto: promedio L/día (7 días) y mini `BarChart` 7 días + "Ver historial completo" -> `animales/{codigo}/produccion`; botón "Registrar producción" -> `produccion/registrar?codigo=` | [Backend] (`ProductionLocal` sin `animalId`) - si no hay soporte, `EmptyState` "El registro por animal estará disponible pronto" |
| Reproducción | `ReproStatusChip` actual + línea de tiempo de eventos + botón "Registrar evento" -> `reproduccion/registrar?codigo=`. Solo para hembras; en machos el tab muestra "No aplica para machos" | [Backend] |

- "Estado / observación" del Figma: `observacion` [Existe]; "Estado" (activo, vendido, muerto) **no existe** -> [Backend] (`estado` en Animal). Mientras tanto, omitir la fila.
- Tab inicial por argumento `tab` de la ruta (04 §3). Tab seleccionado sobrevive a rotación (`rememberSaveable`).
- Potrero actual: chip clicable que abre el Detalle de potrero cuyo `nombre` coincide.
