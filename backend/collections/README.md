# Colección Postman — AgroControl API

Equivalente formal a Insomnia/Postman para la entrega **Backend API funcional**. Swagger UI sigue siendo válido para demostración en vivo.

## Importar en Postman

1. Postman → **Import** → seleccionar `AgroControl.postman_collection.json`.
2. Crear entorno (opcional) con variable `baseUrl` = `http://localhost:3000/api/v1`.
3. Ejecutar carpeta **Auth → Login demo**; el script guarda `accessToken` en variables de colección.
4. Ejecutar el resto de requests (usan Bearer automático).

## Importar en Insomnia

1. **Import/Export** → **From URL** (con API en marcha): `http://localhost:3000/api/docs-json`  
   o importar el JSON de Postman (Insomnia acepta formato Postman v2).

## OpenAPI

Con `npm run start:dev`:

```bash
curl -o openapi/agrocontrol-openapi.json http://localhost:3000/api/docs-json
```

O desde la raíz del backend: `npm run openapi:export` (mismo efecto si el puerto 3000 responde).

## Usuario demo

- Email: `demo@agrocontrol.local`
- Password: `demo1234`

## Alternativa: Swagger

http://localhost:3000/api/docs — ver guion en [`docs/entrega/BACKEND-API.md`](../../docs/entrega/BACKEND-API.md).
