# Pantalla: Registrar evento reproductivo

Ruta: `reproduccion/registrar?codigo={codigo}`. **No existe en Figma**: diseño propio para cubrir los estados del frame 12 (En celo, Inseminadas, Gestantes, Partos). Paquete `ui/feature/reproduccion/registrar`. Sin barra inferior. **Flag `REPRODUCCION` [Backend]**.

Leyenda: [Existe] · [UI] · [Backend].

## Wireframe
```text
┌────────────────────────────────────┐
│ (X) Registrar evento               │
├────────────────────────────────────┤
│ Animal *                           │
│ [ (Buscar) Código o nombre      v ]│  AnimalPicker solo hembras (sexo Hembra)
│   (avatar) Vaca 024 [Inseminada]   │  muestra estado actual
│ Tipo de evento *                   │
│ [(Celo) Celo] [(Inseminacion) Inseminación] │ ChoiceChipGroup 2x2, 56dp
│ [(Diagnostico) Diagnóstico] [(Cria) Parto]  │
│ Fecha *                            │
│ [ (Fecha) 14/10/2026            ]  │
│ ── campos según tipo ──            │
│ Observación                        │
│ [                               ]  │
├────────────────────────────────────┤
│ [         Guardar evento         ] │
└────────────────────────────────────┘
```

## Campos dinámicos por tipo
| Tipo | Campos adicionales | Resultado en estado |
|---|---|---|
| Celo | Intensidad (chips: Leve / Normal / Fuerte), opcional | EN_CELO; sugerencia "Inseminar en 12 horas" |
| Inseminación | Método * (chips: Inseminación artificial / Monta natural); Toro o pajilla (texto, o dropdown de animales tipo Toro para monta); Inseminador (texto, opcional) | INSEMINADA; muestra "Diagnóstico sugerido: {fecha+30 días}" |
| Diagnóstico de gestación | Resultado * (chips: Preñada / Vacía); Días de gestación estimados (número, opcional) | GESTANTE o VACIA; si preñada muestra "Parto probable: {servicio + 283 días}" |
| Parto | Tipo de parto (chips: Normal / Asistido / Cesárea); Cría (chips: Hembra / Macho / Ninguna viva); Observación | PARIDA; tras guardar, `ConfirmDialog` "¿Registrar la cría como nuevo animal?" -> Agregar animal con tipo Ternera/Ternero, sexo y fecha de nacimiento prellenados |

Validaciones de coherencia (advertencias, no bloqueos): diagnóstico sin inseminación previa; parto antes de 260 días desde la inseminación; evento en un macho (bloqueado: el picker solo lista hembras).

## Datos
- Animales: `animalRepository.getAll()` filtrado `sexo == "Hembra"` [Existe].
- Evento: `ReproductionEvent(localId, animalCodigo, tipo: CELO|INSEMINACION|DIAGNOSTICO|PARTO, fecha, datos: Map, observacion, syncState)` [Backend]: entidad Room, repositorio con outbox `entity = "REPRODUCTION"`, `/sync/pull` con eventos, y endpoint de lectura. El backend ya tiene módulo `reproduction` (`docs/architecture/overview.md`); alinear el contrato con él.

## Criterios de aceptación
- [ ] Los campos cambian según el tipo sin perder los comunes.
- [ ] Las fechas calculadas (diagnóstico, parto probable) se muestran antes de guardar.
- [ ] Parto ofrece registrar la cría con datos prellenados.
- [ ] Funciona offline.
