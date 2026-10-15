/**
 * Descarga OpenAPI desde la API en marcha.
 * Uso: npm run start:dev (otra terminal) && npm run openapi:export
 */
import { mkdir, writeFile } from 'node:fs/promises';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const base = process.env.API_BASE ?? 'http://127.0.0.1:3000';
const url = `${base.replace(/\/$/, '')}/api/docs-json`;
const outDir = join(dirname(fileURLToPath(import.meta.url)), '..', 'openapi');
const outFile = join(outDir, 'agrocontrol-openapi.json');

try {
  const res = await fetch(url);
  if (!res.ok) {
    console.error(`Error ${res.status}: ${url}`);
    console.error('Levanta la API con: npm run start:dev');
    process.exit(1);
  }
  const json = await res.text();
  await mkdir(outDir, { recursive: true });
  await writeFile(outFile, json, 'utf8');
  console.log(`OpenAPI guardado en ${outFile}`);
} catch (e) {
  console.error('No se pudo conectar. ¿Está corriendo npm run start:dev?', e.message);
  process.exit(1);
}
