# Memoria del proyecto AgroControl / AgroGest

> **Documentación vigente para entrega y presentación:** [`docs/ENTREGA.md`](docs/ENTREGA.md). Este archivo conserva decisiones tempranas del proyecto.

## Propósito

App para agropecuarios: animales, potreros, productividad (leche, carne, etc.), producción, reportes semanales/mensuales y trabajos pendientes.

## Decisiones técnicas

| Tema | Decisión |
|------|----------|
| Móvil | Android Kotlin, XML + View Binding |
| Backend | NestJS + TypeScript |
| BD servidor | PostgreSQL 16 en Docker |
| ORM | Prisma |
| Móvil local | Room SQLite, offline-first + sync |
| Auth | JWT access + refresh |
| API | `/api/v1` |

## Enlaces

- Figma: https://www.figma.com/design/WeMBtK1xWP0qNTtwllbwcL/AgroGest
- API local: `http://localhost:3000/api/v1`
- Emulador Android → host: `http://10.0.2.2:3000/api/v1`

## Glosario

- **Finca**: explotación agropecuaria (tenant lógico).
- **Finanzas (módulo UI)**: productividad por rubro, no contabilidad ERP.
- **Potrero**: parcela de pastoreo con especies/categorías permitidas.

## Última sesión

Monorepo: `app/`, `backend/`, `infra/`. Ver `todo.md` para fases.
