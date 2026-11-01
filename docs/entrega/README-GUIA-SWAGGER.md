# Guía visual Swagger (Word)

| Archivo | Descripción |
|---------|-------------|
| [`Guia-Pruebas-API-Swagger.docx`](Guia-Pruebas-API-Swagger.docx) | Documento Word con capturas y contexto por endpoint |
| [`swagger-guide-manifest.json`](swagger-guide-manifest.json) | Metadatos (propósito, pantalla Android, HTTP esperado) |
| [`swagger-screenshots/`](swagger-screenshots/) | PNG generados desde Swagger UI |

## Regenerar (API en `localhost:3000`)

```bash
cd docs/entrega/scripts
npm install
node capture-swagger-screenshots.mjs
node build-swagger-guide.mjs
```

Requisitos: `npm run start:dev` en `backend/` y semilla demo (`npm run db:setup`).

Las capturas recortan el **opblock completo** de Swagger (request + response) con interacción real (Try it out / Execute) y se insertan en Word con **escala proporcional** (máx. 620×900 px lógicos), sin deformar el aspecto.
