# Pantalla: Reportes

Ruta: `reportes` (desde Inicio y Más). Reemplaza `ReportesActivity`. Paquete `ui/feature/reportes`.

## Propósito
Resumen semanal y mensual de producción calculado por el servidor.

## Wireframe
```text
┌────────────────────────────────────┐
│ (Volver) Reportes                  │
│ [Semanal][Mensual]                 │  ChoiceChipGroup (TabRow también válido)
│ (<)  7 oct - 14 oct 2026  (>)      │  selector de periodo, flechas 48dp
├────────────────────────────────────┤
│ ┌────────────────────────────────┐ │
│ │ 48 registros                   │ │  KpiTile recordCount
│ └────────────────────────────────┘ │
│ Totales por rubro                  │
│ Leche                 1.540        │  ProgressBarRow
│ Carne                   320        │
│ Actualizado 14/10/2026 16:40       │
└────────────────────────────────────┘
```

## Datos
- Semanal: `api.reportWeekly(from, to)` con `from = hoy - 6 días`, `to = hoy` en formato `yyyy-MM-dd` (`LocalDate.toString()`; corrige el bug actual sin ceros). Flechas mueven la ventana 7 días; no permitir futuro.
- Mensual: `api.reportMonthly(year, month)`; flechas cambian de mes. Etiqueta "Octubre 2026".
- `ReportResponse(period, totalsByProductType: Map<String, Double>, recordCount)`. La respuesta no trae unidad: mostrar solo el número formateado; si el rubro contiene "leche" sufijo "litros".
- Guardar en memoria del ViewModel el último reporte cargado por periodo (no persistir en Room en v1).

## Estados
| Estado | UI |
|---|---|
| Sin internet | `OfflineRequiredState`: "Los reportes necesitan internet. Mientras tanto revisa Producción." + botón "Ir a Producción" |
| Cargando | Esqueleto de KPI + 3 barras |
| Error HTTP | `ErrorState` "No pudimos cargar el reporte" + Reintentar |
| `recordCount == 0` | `EmptyState` "Sin registros en este periodo" |
| 401 | flujo de sesión expirada (04 §4.1) |

## Criterios de aceptación
- [ ] Fechas enviadas con formato `yyyy-MM-dd` válido.
- [ ] Navegar entre periodos sin bloquear la UI.
- [ ] Mensaje claro sin internet; nunca `exception.message`.

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

Se accede desde la tarjeta "Ver reportes" del Inicio (frame 4) y desde Más. Los totales por rubro usan `ProgressBarRow` y, en mensual, un `AgroBarChart` (03 §10) por semana cuando la API devuelva desglose [Backend]; con la respuesta actual (`totalsByProductType`, `recordCount`) [Existe] solo se muestran totales.
