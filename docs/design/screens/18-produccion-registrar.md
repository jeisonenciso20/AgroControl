# Pantalla: Registrar producción de leche

Ruta: `produccion/registrar?codigo={codigo}`. Figma frame 14 (titulado por error "Registrar reproducción" en Figma; aquí "Registrar producción"). Paquete `ui/feature/produccion/registrar`. Sin barra inferior. **Flag `REGISTRAR_PRODUCCION` [Backend]**.

Leyenda: [Existe] · [UI] · [Backend].

## Propósito
Anotar el ordeño de cada vaca en segundos, varias vacas seguidas, en el corral, sin señal.

## Wireframe
```text
┌────────────────────────────────────┐
│ (X) Registrar producción           │
├────────────────────────────────────┤
│ Animal *                           │
│ [ (Buscar) Código o nombre      v ]│  AnimalPicker: dropdown con búsqueda, solo hembras tipo Vaca
│   (avatar) Lucero · 001            │  confirmación visual del elegido
│ Fecha *                            │
│ [ (Fecha) 14/10/2026            ]  │  por defecto hoy, máx hoy
│ Jornada *                          │
│ [ (Manana) Mañana | (Tarde) Tarde ]│  JornadaSelector; auto: antes de 12:00 Mañana
│ Cantidad *                         │
│ [ (Leche) 12,5            litros ] │  NumberField grande (headlineSmall en el valor)
│ Observación                        │
│ [                               ]  │  p. ej. "Mastitis, leche descartada"
├────────────────────────────────────┤
│ [      Guardar y registrar otra  ] │  SecondaryButton 56dp
│ [          Guardar               ] │  PrimaryButton 56dp
└────────────────────────────────────┘
```

## Campos
| Campo | Binding propuesto | Validación | Origen |
|---|---|---|---|
| Animal | `animalCodigo` | Obligatorio; existe en Room | [Backend] (campo nuevo en producción); lista de animales [Existe] |
| Fecha | `fecha` yyyy-MM-dd | Obligatorio, no futura | [Existe] |
| Jornada | `jornada` = MANANA / TARDE | Obligatorio | [Backend] |
| Cantidad | `cantidad` (Double), `unidad` = "litros" fija | > 0 y <= 60 (advertencia, no bloqueo, si > 40: "¿Seguro? Es una cantidad alta") | [Existe] |
| Tipo | `productType` = "Leche" fijo en esta pantalla | - | [Existe] |
| Observación | `notas` | Opcional | [Existe] |

Duplicado: si ya existe registro para animal + fecha + jornada, `ConfirmDialog` "Ya registraste la mañana de Lucero (12 L). ¿Reemplazar?".

Persistencia requerida: `ProductionRepository.insert` (Room + `SyncOutbox(entity = "PRODUCTION", operation = "CREATE")` + `schedule()`), PK local temporal como en ficha 16, campos `animalCodigo` y `jornada` en Room (migración v3), DTO y backend. Offline-first obligatorio.

## Interacciones
- "Guardar y registrar otra": guarda, Snackbar corto "Lucero: 12,5 L guardados", limpia animal y cantidad, conserva fecha y jornada, foco en Animal.
- "Guardar": guarda y vuelve a Producción.
- Animal preseleccionado si llega `codigo`.

## Criterios de aceptación
- [ ] Registrar 5 vacas seguidas sin salir de la pantalla.
- [ ] Funciona en modo avión; los totales del día en Producción se actualizan.
- [ ] Teclado decimal acepta coma.
