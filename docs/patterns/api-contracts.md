# Contratos API

- Base: `/api/v1`
- Auth: `Authorization: Bearer <accessToken>`
- Errores JSON: `{ "statusCode", "message", "error" }`
- Fechas: ISO 8601 (`YYYY-MM-DD` o datetime UTC)
- Paginación (futuro): `?page=1&limit=20`

## Sync

### Push

`POST /sync/push` body:

```json
{
  "changes": [
    {
      "entity": "ANIMAL",
      "operation": "CREATE",
      "localId": "local-uuid",
      "serverId": null,
      "payload": { },
      "clientUpdatedAt": "2026-03-15T12:00:00.000Z"
    }
  ]
}
```

Respuesta: `{ "applied": [{ "localId", "serverId", "entity" }] }`

### Pull

`GET /sync/pull?since=<iso>` → entidades modificadas desde `since` (animales, potreros, production, pendingTasks, reproductionEvents).

### Completar tarea vía sync

```json
{
  "entity": "PENDING_TASK",
  "operation": "UPDATE",
  "localId": "<serverTaskId>",
  "serverId": "<serverTaskId>",
  "payload": { "status": "COMPLETED" },
  "clientUpdatedAt": "..."
}
```

Equivalente HTTP directo: `PATCH /tasks/:id/complete`.

## Entidades push (resumen)

| entity | CREATE | UPDATE |
|--------|--------|--------|
| ANIMAL | Sí | Sí |
| POTRERO | Sí | Sí |
| PRODUCTION_RECORD | Sí | — |
| PENDING_TASK | Sí | Sí (COMPLETED) |
| REPRODUCTION_EVENT | Sí | — |

Catálogo completo: [`docs/entrega/BACKEND-API.md`](../entrega/BACKEND-API.md).
