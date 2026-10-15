import { PrismaClient, ProductType, TaskStatus, TaskType } from '@prisma/client';
import * as bcrypt from 'bcrypt';

const prisma = new PrismaClient();

const FINCA_ID = 'seed-finca-1';
const USER_ID = 'seed-user-1';

const POTREROS = [
  { id: 'seed-potrero-1', nombre: 'Potrero 1', area: 2.5, especies: ['Vaca', 'Ternera'] },
  { id: 'seed-potrero-2', nombre: 'Potrero 2', area: 3.0, especies: ['Vaca', 'Toro'] },
  { id: 'seed-potrero-3', nombre: 'Potrero 3', area: 1.8, especies: ['Vaca'] },
] as const;

const ANIMALES = [
  {
    id: 'seed-animal-001',
    codigo: '001',
    nombre: 'Lucero',
    tipo: 'Vaca',
    raza: 'Holstein',
    sexo: 'Hembra',
    peso: 480,
    fecha: '2020-03-15',
    potrero: 'Potrero 1',
    potreroId: 'seed-potrero-1',
    observacion: 'Alta producción lechera',
  },
  {
    id: 'seed-animal-002',
    codigo: '002',
    nombre: '',
    tipo: 'Vaca',
    raza: 'Jersey',
    sexo: 'Hembra',
    peso: 420,
    fecha: '2019-08-22',
    potrero: 'Potrero 2',
    potreroId: 'seed-potrero-2',
    observacion: '',
  },
  {
    id: 'seed-animal-003',
    codigo: '003',
    nombre: '',
    tipo: 'Toro',
    raza: 'Brahman',
    sexo: 'Macho',
    peso: 650,
    fecha: '2018-01-10',
    potrero: 'Potrero 3',
    potreroId: 'seed-potrero-3',
    observacion: 'Reproductor principal',
  },
  {
    id: 'seed-animal-024',
    codigo: '024',
    nombre: 'Estrella',
    tipo: 'Vaca',
    raza: 'Holstein',
    sexo: 'Hembra',
    peso: 455,
    fecha: '2021-06-12',
    potrero: 'Potrero 2',
    potreroId: 'seed-potrero-2',
    observacion: 'Inseminación programada',
  },
] as const;

function daysAgo(n: number): Date {
  const d = new Date();
  d.setDate(d.getDate() - n);
  d.setHours(0, 0, 0, 0);
  return d;
}

async function main() {
  const finca = await prisma.finca.upsert({
    where: { id: FINCA_ID },
    update: { nombre: 'Finca La Esperanza' },
    create: { id: FINCA_ID, nombre: 'Finca La Esperanza' },
  });

  const passwordHash = await bcrypt.hash('demo1234', 10);
  await prisma.user.upsert({
    where: { email: 'demo@agrocontrol.local' },
    update: { nombre: 'Ganadero Demo', passwordHash, fincaId: finca.id },
    create: {
      id: USER_ID,
      email: 'demo@agrocontrol.local',
      passwordHash,
      nombre: 'Ganadero Demo',
      fincaId: finca.id,
    },
  });

  for (const p of POTREROS) {
    await prisma.potrero.upsert({
      where: { id: p.id },
      update: {
        areaHectareas: p.area,
        especiesPermitidas: [...p.especies],
        estado: 'ACTIVO',
      },
      create: {
        id: p.id,
        fincaId: finca.id,
        nombre: p.nombre,
        areaHectareas: p.area,
        especiesPermitidas: [...p.especies],
      },
    });
  }

  for (const a of ANIMALES) {
    await prisma.animal.upsert({
      where: { id: a.id },
      update: {
        nombre: a.nombre,
        tipo: a.tipo,
        raza: a.raza,
        sexo: a.sexo,
        peso: a.peso,
        fechaNacimiento: new Date(a.fecha),
        potreroNombre: a.potrero,
        observacion: a.observacion,
      },
      create: {
        id: a.id,
        fincaId: finca.id,
        codigo: a.codigo,
        nombre: a.nombre,
        tipo: a.tipo,
        raza: a.raza,
        sexo: a.sexo,
        peso: a.peso,
        fechaNacimiento: new Date(a.fecha),
        potreroNombre: a.potrero,
        observacion: a.observacion,
      },
    });

    await prisma.animalPotrero.upsert({
      where: { id: `seed-ap-${a.codigo}` },
      update: { potreroId: a.potreroId, hasta: null },
      create: {
        id: `seed-ap-${a.codigo}`,
        animalId: a.id,
        potreroId: a.potreroId,
      },
    });
  }

  const produccion = [
    { id: 'seed-prod-1', type: ProductType.LECHE, cantidad: 62, unidad: 'L', days: 1, animalId: 'seed-animal-001' },
    { id: 'seed-prod-2', type: ProductType.LECHE, cantidad: 58, unidad: 'L', days: 3, animalId: 'seed-animal-001' },
    { id: 'seed-prod-3', type: ProductType.LECHE, cantidad: 66, unidad: 'L', days: 5, animalId: 'seed-animal-024' },
    { id: 'seed-prod-4', type: ProductType.CARNE, cantidad: 120, unidad: 'kg', days: 7, animalId: null },
    { id: 'seed-prod-5', type: ProductType.HUEVO, cantidad: 180, unidad: 'unidades', days: 14, animalId: null },
    { id: 'seed-prod-6', type: ProductType.LECHE, cantidad: 55, unidad: 'L', days: 21, animalId: 'seed-animal-002' },
  ] as const;

  for (const p of produccion) {
    await prisma.productionRecord.upsert({
      where: { id: p.id },
      update: {
        cantidad: p.cantidad,
        fecha: daysAgo(p.days),
        notas: 'Dato semilla',
      },
      create: {
        id: p.id,
        fincaId: finca.id,
        animalId: p.animalId,
        productType: p.type,
        cantidad: p.cantidad,
        unidad: p.unidad,
        fecha: daysAgo(p.days),
        notas: 'Dato semilla',
      },
    });
  }

  const repro = [
    {
      id: 'seed-repro-1',
      animalId: 'seed-animal-024',
      tipo: 'INSEMINACION' as const,
      programada: '2026-10-02',
    },
    {
      id: 'seed-repro-2',
      animalId: 'seed-animal-002',
      tipo: 'CELO' as const,
      programada: '2026-10-20',
    },
    {
      id: 'seed-repro-3',
      animalId: 'seed-animal-001',
      tipo: 'DIAGNOSTICO' as const,
      programada: '2026-11-05',
    },
  ] as const;

  for (const r of repro) {
    await prisma.reproductionEvent.upsert({
      where: { id: r.id },
      update: {
        tipoEvento: r.tipo,
        programada: new Date(r.programada),
      },
      create: {
        id: r.id,
        fincaId: finca.id,
        animalId: r.animalId,
        tipoEvento: r.tipo,
        programada: new Date(r.programada),
      },
    });
  }

  const tareas = [
    {
      id: 'seed-task-1',
      titulo: 'Fumigación pendiente — Potrero 3',
      tipo: TaskType.POTRERO,
      status: TaskStatus.PENDING,
      vencimiento: '2026-10-03',
      referencia: 'Potrero 3',
    },
    {
      id: 'seed-task-2',
      titulo: 'Necesita abono — Potrero 2',
      tipo: TaskType.POTRERO,
      status: TaskStatus.PENDING,
      vencimiento: '2026-10-05',
      referencia: 'Potrero 2',
    },
    {
      id: 'seed-task-3',
      titulo: 'Inseminación programada — Vaca 024',
      tipo: TaskType.ANIMAL,
      status: TaskStatus.PENDING,
      vencimiento: '2026-10-02',
      referencia: '024',
    },
    {
      id: 'seed-task-4',
      titulo: 'Vacunación general — completada',
      tipo: TaskType.GENERAL,
      status: TaskStatus.COMPLETED,
      vencimiento: '2026-09-15',
      referencia: 'Finca',
    },
  ] as const;

  for (const t of tareas) {
    await prisma.pendingTask.upsert({
      where: { id: t.id },
      update: {
        titulo: t.titulo,
        status: t.status,
        vencimiento: new Date(t.vencimiento),
      },
      create: {
        id: t.id,
        fincaId: finca.id,
        titulo: t.titulo,
        tipo: t.tipo,
        status: t.status,
        vencimiento: new Date(t.vencimiento),
        referencia: t.referencia,
      },
    });
  }

  const [animales, potreros, prod, tasks, events] = await Promise.all([
    prisma.animal.count({ where: { fincaId: finca.id } }),
    prisma.potrero.count({ where: { fincaId: finca.id } }),
    prisma.productionRecord.count({ where: { fincaId: finca.id } }),
    prisma.pendingTask.count({ where: { fincaId: finca.id } }),
    prisma.reproductionEvent.count({ where: { fincaId: finca.id } }),
  ]);

  console.log('Seed OK');
  console.log('  Login: demo@agrocontrol.local / demo1234');
  console.log(`  Animales: ${animales}, Potreros: ${potreros}, Producción: ${prod}, Tareas: ${tasks}, Reproducción: ${events}`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
