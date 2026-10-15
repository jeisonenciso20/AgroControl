/**
 * Genera Guia-Pruebas-API-Swagger.docx desde manifest + PNG.
 * Uso: node build-swagger-guide.mjs
 */
import { readFileSync, existsSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import {
  Document,
  Packer,
  Paragraph,
  TextRun,
  HeadingLevel,
  ImageRun,
  AlignmentType,
  PageBreak,
} from 'docx';

const __dir = dirname(fileURLToPath(import.meta.url));
const ROOT = join(__dir, '..');
const SHOTS = join(ROOT, 'swagger-screenshots');
const MANIFEST = JSON.parse(readFileSync(join(ROOT, 'swagger-guide-manifest.json'), 'utf8'));
const OUT_DOC = join(ROOT, 'Guia-Pruebas-API-Swagger.docx');

const MAX_WIDTH = 620;
const MAX_HEIGHT = 900;

function readPngSize(buffer) {
  return { width: buffer.readUInt32BE(16), height: buffer.readUInt32BE(20) };
}

function fitImageTransform(naturalWidth, naturalHeight) {
  let w = naturalWidth;
  let h = naturalHeight;
  if (w > MAX_WIDTH) {
    h = Math.round(h * (MAX_WIDTH / w));
    w = MAX_WIDTH;
  }
  if (h > MAX_HEIGHT) {
    w = Math.round(w * (MAX_HEIGHT / h));
    h = MAX_HEIGHT;
  }
  return { width: w, height: h };
}

function imgParagraph(filename) {
  const path = join(SHOTS, filename);
  if (!existsSync(path)) {
    return new Paragraph({
      children: [new TextRun({ text: `[Falta captura: ${filename}]`, italics: true, color: 'CC0000' })],
    });
  }
  const data = readFileSync(path);
  const { width: nw, height: nh } = readPngSize(data);
  const transform = fitImageTransform(nw, nh);
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    children: [
      new ImageRun({
        data,
        transformation: transform,
        type: 'png',
      }),
    ],
    spacing: { after: 200 },
  });
}

function p(text, opts = {}) {
  return new Paragraph({
    children: [new TextRun({ text, ...opts })],
    spacing: { after: 120 },
  });
}

function heading(text, level = HeadingLevel.HEADING_1) {
  return new Paragraph({ text, heading: level, spacing: { before: 240, after: 120 } });
}

const children = [
  heading('Guía de pruebas — API AgroControl (Swagger UI)'),
  p(`Proyecto: ${MANIFEST.project}`),
  p(`Fecha de generación: ${new Date().toLocaleDateString('es-CO', { dateStyle: 'long' })}`),
  p(`URL Swagger: ${MANIFEST.swaggerUrl}`),
  p('Requisitos: Docker (PostgreSQL), npm run db:setup, npm run start:dev en backend/.'),
  p(`Usuario demo: ${MANIFEST.demoUser} / demo1234`),
  new Paragraph({ children: [new PageBreak()] }),
  heading('1. Autenticación en Swagger', HeadingLevel.HEADING_1),
];

const intro = MANIFEST.sections.find((s) => s.kind === 'intro');
if (intro) {
  children.push(heading(intro.title, HeadingLevel.HEADING_2));
  children.push(p(intro.purpose));
  children.push(p(`Pasos: ${intro.steps}`));
  children.push(p(`Resultado esperado: ${intro.expectedStatus}`));
  children.push(imgParagraph(intro.screenshot));
}

children.push(heading('2. Endpoints probados', HeadingLevel.HEADING_1));

for (const section of MANIFEST.sections.filter((s) => s.method)) {
  const title = `${section.method} ${section.path}`;
  children.push(heading(title, HeadingLevel.HEADING_2));
  children.push(p(`Propósito: ${section.purpose}`));
  if (section.androidScreen && section.androidScreen !== '—') {
    children.push(p(`Pantalla Android: ${section.androidScreen}`));
  }
  children.push(
    p(
      'Pasos: Expandir la operación en Swagger → Try it out → completar parámetros/body → Execute → revisar código de respuesta y cuerpo JSON.',
    ),
  );
  children.push(p(`Resultado esperado: HTTP ${section.expectedStatus}.`));
  if (section.query) {
    children.push(p(`Query: ${JSON.stringify(section.query)}`));
  }
  children.push(imgParagraph(section.screenshot));
}

const doc = new Document({
  sections: [{ properties: {}, children }],
});

const buffer = await Packer.toBuffer(doc);
await import('node:fs/promises').then((fs) => fs.writeFile(OUT_DOC, buffer));
console.log(`Documento: ${OUT_DOC}`);
