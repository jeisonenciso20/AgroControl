# Diseño — AgroGest / AgroControl

> **Documentación vigente para entrega:** [`docs/ENTREGA.md`](docs/ENTREGA.md). Especificación UI actual en [`docs/design/00-README.md`](docs/design/00-README.md).

Referencia Figma: [AgroGest](https://www.figma.com/design/WeMBtK1xWP0qNTtwllbwcL/AgroGest?node-id=0-1)

Detalle de mapeo pantalla ↔ código: [docs/DESIGN_FIGMA_MAPPING.md](docs/DESIGN_FIGMA_MAPPING.md)

## Tokens principales

| Token | Hex |
|-------|-----|
| primary | `#21852A` |
| textPrimary | `#26352A` |
| surfaceList | `#F5F7F5` |
| kpiAnimales | `#91DF87` |
| kpiProduccion | `#A8D9FF` |
| kpiPotreros | `#E7FF80` |
| kpiReproduccion | `#F1B6B9` |
| kpiReportes | `#C9A8E5` |

## Pantallas MVP

Inicio, Animales (lista/alta/detalle/edición), Potreros, Producción, Reproducción, Finanzas (productividad), Reportes, Pendientes, Más (perfil/sync/logout).

## Componentes

- Tarjeta animal: `item_animal_card.xml`
- Bottom nav 5 ítems: `activity_main.xml`

## Nueva especificación de diseño (Compose + Material 3)

La especificación vigente del frontend está en [docs/design/00-README.md](docs/design/00-README.md): sistema de diseño, migración de XML a Jetpack Compose, catálogo de componentes, navegación y una ficha por pantalla. Los tokens de esta página quedan como referencia histórica; ante cualquier diferencia, manda `docs/design/01-design-system.md`.
