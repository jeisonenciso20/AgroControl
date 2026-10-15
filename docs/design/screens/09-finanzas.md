# Pantalla: Finanzas (productividad por rubro)

Ruta: `finanzas` (desde Inicio y Más). Reemplaza `FinanzasActivity`. Paquete `ui/feature/finanzas`.

Nota de dominio (`Memoria.md`): "Finanzas" es **productividad por rubro**, no contabilidad. No hay precios ni montos en los datos; no inventar valores en pesos.

## Propósito
Comparar cuánto produce la finca por rubro y cómo evoluciona.

## Wireframe
```text
┌────────────────────────────────────┐
│ (Volver) Productividad             │  título visible "Productividad"; etiqueta de menú "Finanzas"
│ [Este mes][Mes anterior][Todo]     │  ChoiceChipGroup
├────────────────────────────────────┤
│ Total por rubro                    │
│ Leche                 1.540 litros │  ProgressBarRow (barra relativa al mayor de igual unidad)
│ ██████████████████░░░░             │
│ Carne                   320 kg     │
│ ██████░░░░░░░░░░░░░░░░             │
│                                    │
│ Por semana (Leche)                 │  selector de rubro (dropdown)
│ Sem 1  ███████████   380 litros    │  ProgressBarRow por semana del periodo
│ Sem 2  █████████████ 420 litros    │
│ ...                                │
│ Basado en 48 registros             │  bodySmall
└────────────────────────────────────┘
```
(Las barras del esquema representan `ProgressBarRow`; no son caracteres.)

## Datos
- Fuente: `LocalCacheDao.allProduction()` (igual que hoy), filtrado por periodo según `fecha`.
- Agregación (misma lógica actual, ampliada): `groupBy { productType to unidad }.mapValues { sumOf { cantidad } }`.
- Por semana: agrupar por semana ISO de `fecha` para el rubro seleccionado.
- Sin librería de gráficos: barras con `Box` + `fillMaxWidth(fraction)`. Cada barra con texto numérico (no depender de la longitud visual).

## Estados
- Vacío: `EmptyState(Finanzas, "Aún no hay datos de producción", "Sincroniza para ver la productividad de tu finca.", "Sincronizar ahora")`.

## Criterios de aceptación
- [ ] Totales coinciden con la agregación de la Activity original para "Todo".
- [ ] No mezcla unidades.
- [ ] Cada barra es leída por TalkBack con rubro y valor.

## Actualización Figma (prevalece donde difiera)

Leyenda: [Existe] modelo/API actual · [UI] solo interfaz o preferencia local · [Backend] requiere campo/endpoint/outbox nuevo (ver 00-README §6.3).

El Figma ubica Finanzas como tab de la barra inferior; aquí se mueve a **Más** (decisión en 04 §2). No hay frame Figma con contenido de Finanzas, por lo que se mantiene esta ficha. Las barras pasan a usar el componente `AgroBarChart` (03 §10) para la vista "Por semana".
