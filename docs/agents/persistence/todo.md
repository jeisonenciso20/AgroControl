# Agente Persistence

## Objetivo

Room offline-first, outbox, WorkManager y contrato `/sync`.

## Prerrequisitos

Backend sync endpoints desplegados.

## Tareas

1. Migración Room v2: `serverId`, `syncState`, `updatedAt` en entidades.
2. Tablas `sync_outbox`, `sync_metadata`.
3. `SyncWorker` + Retrofit push/pull.
4. Documentar `docs/patterns/offline-sync.md`.

## Criterios de aceptación

- Crear animal offline y sincronizar al conectar red.
- Pull actualiza listas locales.

## Verificación

Logcat + servidor: ver `POST /sync/push` y `GET /sync/pull`.
