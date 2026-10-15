# Pantalla: Historial de producción por animal

Ruta: `animales/{codigo}/produccion` (desde Detalle de animal > tab Producción y desde `MilkingCard`). Figma frame 11. Paquete `ui/feature/produccion/historial`. **Flag `PRODUCCION_POR_ANIMAL` [Backend]**.

Leyenda: [Existe] · [UI] · [Backend].

## Wireframe
```text
┌────────────────────────────────────┐
│ (Volver) Producción · Lucero       │
│ (avatar 40dp) Vaca · 001 · Holstein│
├────────────────────────────────────┤
│ [ Semana | Mes | Año ]             │  PeriodToggle (03 §10)
│ (<)   8 oct - 14 oct 2026   (>)    │  navegación de periodo, flechas 48dp
│ ┌────────────────────────────────┐ │
│ │ 22,4 L/día                     │ │  displaySmall: promedio del periodo
│ │ Promedio · Total 157 L         │ │  bodyMedium
│ │ +6% vs semana anterior (Tendencia)│ │ success/error con icono y texto
│ └────────────────────────────────┘ │
│ Litros diarios                     │
│ ┌────────────────────────────────┐ │
│ │  ▇ ▇ ▆ ▇ █ ▇ █   (AgroBarChart)│ │  barras por día; línea de promedio
│ │  Mi Ju Vi Sá Do Lu Ma          │ │
│ └────────────────────────────────┘ │
│ Registros recientes                │
│ 14/10  Mañana 12 L · Tarde 10 L  22 L│ AgroListItem por día
│ 13/10  Mañana 11 L · Tarde 11 L  22 L│
│ ...                                │
│          [+ Registrar producción]  │  AgroFab -> produccion/registrar?codigo=
└────────────────────────────────────┘
```
(Los bloques del esquema representan el gráfico, no caracteres.)

## Agregación
| Periodo | Barras | Etiqueta X |
|---|---|---|
| Semana | 7 barras, litros por día | Lu, Ma, Mi... |
| Mes | 28-31 barras por día (scroll horizontal) | 1..31 |
| Año | 12 barras, total mensual; el promedio se muestra como L/día del año | Ene..Dic |

- Promedio L/día = total / días del periodo con al menos un registro (indicar "sobre 6 días con registro").
- Comparación con el periodo anterior: solo si ambos tienen datos.

## Datos
- Registros de `ProductionLocal` con `productType` leche y `animalCodigo == codigo` [Backend: añadir `animalCodigo` y `jornada` a `ProductionLocal`/`ProductionDto`, migración Room v3]. El servidor ya modela `Animal ||--o{ ProductionRecord : optional` (`docs/architecture/database.md`), por lo que el vínculo existe del lado del backend; falta exponerlo en el DTO.
- Animal: `findByCodigo` [Existe].
- Consulta en ViewModel; si el volumen crece, añadir DAO filtrado por animal y rango (adición de persistencia).

## Estados
- Flag desactivado: no hay puntos de entrada; el tab Producción del detalle muestra `EmptyState` informativo.
- Sin registros en el periodo: gráfico vacío compacto + "Sin registros en este periodo" + botón registrar.
- Machos/terneros: no se ofrece el acceso (solo hembras con tipo Vaca).

## Criterios de aceptación
- [ ] Cambiar Semana/Mes/Año recalcula gráfico, promedio y lista.
- [ ] El gráfico tiene resumen accesible y la lista textual muestra los mismos datos.
- [ ] Días sin ordeño se ven como 0.
