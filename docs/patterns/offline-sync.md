# Sincronización offline-first (v1)

1. Escritura → Room + fila en `sync_outbox`.
2. WorkManager envía `POST /sync/push` cuando hay red.
3. `GET /sync/pull?since=` actualiza entidades locales.

**Conflictos:** last-write-wins por `updatedAt` del servidor.

Entidades sync v1: Animal, ProductionRecord, PendingTask. Potreros: pull desde servidor.
