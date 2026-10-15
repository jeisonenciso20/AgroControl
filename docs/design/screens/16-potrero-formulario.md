# Pantalla: Agregar / editar potrero

Rutas: `potreros/nuevo`, `potreros/{serverId}/editar`. Figma frame 10. Paquete `ui/feature/potreros/formulario`. Sin barra inferior. **Flag `CREAR_POTRERO` [Backend]**: hoy los potreros solo llegan por pull (`docs/patterns/offline-sync.md`).

Leyenda: [Existe] · [UI] · [Backend].

## Wireframe
```text
┌────────────────────────────────────┐
│ (X) Nuevo potrero                  │
├────────────────────────────────────┤
│ Nombre del potrero *               │
│ [ (Potrero) Potrero La Ceiba    ]  │
│ Área *                             │
│ [ (Area) 5,2                ha  ]  │  NumberField
│ Tipo de pasto                      │
│ [ (Pasto) Brachiaria         v  ]  │  AgroDropdownField + "Otro" con texto
│ Estado actual *                    │
│ [Disponible][En recuperación]      │  ChoiceChipGroup
│ [No disponible]                    │
│ Animales permitidos                │
│ [Vaca][Toro][Ternero][Ternera]     │  multi-selección FilterChip
│ Fecha de última siembra            │
│ [ (Fecha) dd/mm/aaaa            ]  │  DateField, máx hoy
│ Fecha de próximo uso               │
│ [ (Fecha) dd/mm/aaaa            ]  │  DateField, mín hoy
│ Observación                        │
│ [                               ]  │
├────────────────────────────────────┤
│ [        Guardar potrero       ]   │
└────────────────────────────────────┘
```

## Campos
| Campo | Binding propuesto | Origen |
|---|---|---|
| Nombre | `nombre` (único por finca; "Ya existe un potrero con ese nombre") | [Existe] en modelo |
| Área (ha) | `areaHectareas` > 0 | [Existe] |
| Estado actual | `estado` = `DISPONIBLE` / `RECUPERACION` / `NO_DISPONIBLE` | [Existe] campo, enum [Backend] |
| Animales permitidos | `especiesPermitidas` (List<String>) | [Existe] |
| Tipo de pasto | `tipoPasto`: Brachiaria, Estrella, Kikuyo, Guinea, Angleton, Pangola, Otro | [Backend] |
| Fecha última siembra | `fechaUltimaSiembra` yyyy-MM-dd | [Backend] |
| Fecha próximo uso | `fechaProximoUso` yyyy-MM-dd, >= hoy | [Backend] |
| Observación | `observacion` | [Backend] |

Persistencia requerida (agente persistence): `PotreroLocal` con `syncState`, `localId` (hoy la PK es `serverId` NOT NULL: usar id temporal `local-<uuid>` hasta que el push devuelva el `serverId`), `PotreroRepository.insert/update` encolando `SyncOutbox(entity = "POTRERO", operation = CREATE|UPDATE)`; backend aceptando `POTRERO` en `/sync/push`. La UI no escribe en `LocalCacheDao` directamente.

## Validación e interacciones
Igual que el formulario de animal (05): validación completa al guardar, errores en línea, confirmación al descartar, Snackbar "Potrero guardado en el teléfono..." al volver.

## Criterios de aceptación
- [ ] Oculto mientras `CREAR_POTRERO = false`.
- [ ] Con soporte: crear offline, aparece en la lista con "Por enviar" y sincroniza.
- [ ] Fechas con selector; área con teclado decimal.
