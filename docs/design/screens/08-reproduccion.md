# Pantalla: Reproducción

Ruta: `reproduccion` (desde Inicio y Más). Reemplaza `ReproduccionActivity` (hoy solo texto estático). Paquete `ui/feature/reproduccion`.

## Propósito
Seguimiento de eventos reproductivos (celo, inseminación/monta, diagnóstico de preñez, parto). En v1 la app **no tiene datos locales ni endpoint en `ApiService`** para reproducción (el backend tiene módulo `reproduction`), por lo que la pantalla muestra el resumen disponible y un estado honesto.

## Wireframe v1
```text
┌────────────────────────────────────┐
│ (Volver) Reproducción              │
├────────────────────────────────────┤
│ ┌────────────────────────────────┐ │
│ │ (Reprod) 5                     │ │  KpiTile: DashboardSummary.eventosReproduccion
│ │ Eventos registrados            │ │  Unavailable si no hay red
│ └────────────────────────────────┘ │
│ ┌────────────────────────────────┐ │
│ │ (Info) El detalle de eventos   │ │  AgroCard informativa (tertiaryContainer)
│ │ estará disponible en una       │ │
│ │ próxima versión.               │ │
│ └────────────────────────────────┘ │
│ Hembras del hato                   │  SectionHeader
│ (B) Lucero · Vaca · 6 años     >   │  AnimalListItem filtrado sexo == "Hembra"
│ ...                                │  -> Detalle de animal
└────────────────────────────────────┘
```
La lista de hembras (`animalRepository.getAll().filter { it.sexo.equals("Hembra", true) }`) da valor inmediato con datos existentes.

## Diseño objetivo v2 (cuando exista `GET /reproduction` + caché Room)
- Línea de tiempo por animal: `TimelineItem(icono, tipoEvento, fecha, nota)` con iconos `Reproduccion` (celo/servicio), `Info` (diagnóstico), `Cria` (parto).
- Filtros: Próximos partos (fecha probable = servicio + 283 días en bovinos), Por diagnosticar, Todos.
- FAB "Registrar evento" con formulario: animal (dropdown de hembras), tipo (chips), fecha, observación.
Agregar estos componentes a 03 cuando se implemente.

## Criterios de aceptación (v1)
- [ ] Sin texto técnico ("Conecta el backend para listar desde /reproduction" desaparece).
- [ ] KPI muestra valor con red y "-" con mensaje sin red.
- [ ] Lista de hembras navega al detalle.

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

Frame 12 "Reproducción". Este diseño reemplaza el "Diseño objetivo v2" y se activa con el flag `REPRODUCCION` cuando exista el soporte [Backend]: `GET /reproduction` (o incluido en `/sync/pull`), entidad Room `reproduction_events (serverId, animalCodigo, tipo, fecha, datosJson, syncState)`, sync `entity = "REPRODUCTION"`.

```text
┌────────────────────────────────────┐
│ (Volver) Reproducción              │
│ [ (Buscar) Buscar por código o nombre ] │
│ ┌────────┐┌──────────┐┌─────────┐┌──────┐│  KpiTile x4 (FlowRow 2x2), cada uno es filtro
│ │En celo ││Inseminadas││Gestantes││Partos││
│ │   3    ││    5     ││   12    ││  2   ││
│ └────────┘└──────────┘└─────────┘└──────┘│
│ ┌────────────────────────────────┐ │
│ │ (B) Vaca 024        [Inseminada]│ │  AgroListItem + ReproStatusChip
│ │ Inseminación · 01/10/2026      │ │  último evento + fecha
│ │ Revisar gestación: 31/10       │ │  próxima acción calculada
│ └────────────────────────────────┘ │
│              [+ Registrar evento]  │  AgroFab -> ficha 19
└────────────────────────────────────┘
```

Estados reproductivos (`ReproStatus`, derivados del último evento de cada hembra): VACIA (sin evento o diagnóstico negativo), EN_CELO (celo en últimos 3 días), INSEMINADA (servicio sin diagnóstico), GESTANTE (diagnóstico positivo), PARIDA (parto en últimos 45 días). Próxima acción: inseminada -> diagnóstico a los 30 días; gestante -> parto probable a los 283 días del servicio.

KPI "Partos" = partos del mes en curso; tocar un KPI aplica ese filtro. Lista base: hembras de `animalRepository.getAll()` [Existe] unidas a eventos [Backend]. Sin soporte backend se mantiene la versión v1 descrita arriba.
