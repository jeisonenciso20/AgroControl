import { ExecutionContext, INestApplication, ValidationPipe } from '@nestjs/common';
import { Test, TestingModule } from '@nestjs/testing';
import * as bcrypt from 'bcrypt';
import { AppModule } from '../../src/app.module';
import { JwtAuthGuard } from '../../src/auth/jwt-auth.guard';
import { AuthUser } from '../../src/common/current-user.decorator';
import { PrismaService } from '../../src/prisma/prisma.service';

export const TEST_FINCA_ID = 'finca-test-id';
export const TEST_USER_ID = 'user-test-id';
export const TEST_ENTITY_ID = '00000000-0000-4000-8000-000000000001';

export const TEST_AUTH_USER: AuthUser = {
  userId: TEST_USER_ID,
  fincaId: TEST_FINCA_ID,
  email: 'test@agrocontrol.local',
};

const TEST_PASSWORD = 'password123';

let passwordHashPromise: Promise<string> | null = null;

function getPasswordHash() {
  if (!passwordHashPromise) {
    passwordHashPromise = bcrypt.hash(TEST_PASSWORD, 10);
  }
  return passwordHashPromise;
}

export function createPrismaMock() {
  const now = new Date('2026-01-15T12:00:00.000Z');
  const base = {
    id: TEST_ENTITY_ID,
    fincaId: TEST_FINCA_ID,
    createdAt: now,
    updatedAt: now,
  };

  const mockAnimal = {
    ...base,
    codigo: 'V001',
    nombre: 'Test',
    tipo: 'Vaca',
    raza: 'Holstein',
    sexo: 'Hembra',
    peso: 450,
    fechaNacimiento: new Date('2020-01-01'),
    potreroNombre: 'Potrero 1',
    observacion: '',
    syncVersion: 1,
  };

  const mockPotrero = {
    ...base,
    nombre: 'Potrero 1',
    areaHectareas: 2.5,
    estado: 'DISPONIBLE',
    tipoPasto: 'Brachiaria',
    fechaUltimaSiembra: null,
    fechaProximoUso: null,
    observacion: '',
    especiesPermitidas: ['Vaca'],
    syncVersion: 1,
  };

  const mockProduction = {
    ...base,
    productType: 'LECHE',
    cantidad: 10,
    unidad: 'L',
    fecha: new Date('2026-01-10'),
    jornada: 'MANANA',
    animalId: TEST_ENTITY_ID,
    notas: '',
    syncVersion: 1,
  };

  const mockReproduction = {
    ...base,
    animalId: TEST_ENTITY_ID,
    tipoEvento: 'INSEMINACION',
    programada: new Date('2026-02-01'),
    realizada: null,
    notas: '',
    datosJson: {},
    syncVersion: 1,
  };

  const mockTask = {
    ...base,
    titulo: 'Tarea test',
    tipo: 'GENERAL',
    status: 'PENDING',
    vencimiento: new Date('2026-02-01'),
    referencia: '',
    workType: null,
    observacion: '',
    syncVersion: 1,
  };

  const mockFinca = { id: TEST_FINCA_ID, nombre: 'Finca Test', createdAt: now, updatedAt: now };

  return {
    $connect: jest.fn(),
    $disconnect: jest.fn(),
    user: {
      findUnique: jest.fn(async (args: { where: { email?: string; id?: string }; include?: { finca: boolean } }) => {
        const hash = await getPasswordHash();
        const user = {
          id: TEST_USER_ID,
          email: 'test@agrocontrol.local',
          passwordHash: hash,
          nombre: 'Usuario Test',
          fincaId: TEST_FINCA_ID,
          finca: mockFinca,
          createdAt: now,
          updatedAt: now,
        };
        if (args.where.email && args.where.email !== user.email) return null;
        if (args.where.id && args.where.id !== TEST_USER_ID) return null;
        if (args.include?.finca) return user;
        const { finca: _f, ...withoutFinca } = user;
        return withoutFinca;
      }),
      create: jest.fn().mockResolvedValue({
        id: TEST_USER_ID,
        email: 'new@agrocontrol.local',
        fincaId: TEST_FINCA_ID,
      }),
    },
    finca: {
      create: jest.fn().mockResolvedValue(mockFinca),
    },
    animal: {
      findMany: jest.fn().mockResolvedValue([]),
      findFirst: jest.fn().mockResolvedValue(mockAnimal),
      create: jest.fn().mockResolvedValue(mockAnimal),
      update: jest.fn().mockResolvedValue(mockAnimal),
      delete: jest.fn().mockResolvedValue(mockAnimal),
      upsert: jest.fn().mockResolvedValue(mockAnimal),
      count: jest.fn().mockResolvedValue(0),
    },
    potrero: {
      findMany: jest.fn().mockResolvedValue([]),
      findFirst: jest.fn().mockResolvedValue(mockPotrero),
      create: jest.fn().mockResolvedValue(mockPotrero),
      update: jest.fn().mockResolvedValue(mockPotrero),
      delete: jest.fn().mockResolvedValue(mockPotrero),
      count: jest.fn().mockResolvedValue(0),
    },
    productionRecord: {
      findMany: jest.fn().mockResolvedValue([]),
      findFirst: jest.fn().mockResolvedValue(mockProduction),
      create: jest.fn().mockResolvedValue(mockProduction),
      update: jest.fn().mockResolvedValue(mockProduction),
      delete: jest.fn().mockResolvedValue(mockProduction),
      aggregate: jest.fn().mockResolvedValue({ _sum: { cantidad: 0 } }),
    },
    reproductionEvent: {
      findMany: jest.fn().mockResolvedValue([]),
      findFirst: jest.fn().mockResolvedValue(mockReproduction),
      create: jest.fn().mockResolvedValue(mockReproduction),
      update: jest.fn().mockResolvedValue(mockReproduction),
      delete: jest.fn().mockResolvedValue(mockReproduction),
      count: jest.fn().mockResolvedValue(0),
    },
    pendingTask: {
      findMany: jest.fn().mockResolvedValue([]),
      findFirst: jest.fn().mockResolvedValue(mockTask),
      create: jest.fn().mockResolvedValue(mockTask),
      update: jest.fn().mockResolvedValue({ ...mockTask, status: 'COMPLETED' }),
      delete: jest.fn().mockResolvedValue(mockTask),
    },
  };
}

export type CreateTestAppOptions = {
  mockJwtGuard?: boolean;
};

export async function createTestApp(options: CreateTestAppOptions = {}): Promise<{
  app: INestApplication;
  moduleRef: TestingModule;
  prisma: ReturnType<typeof createPrismaMock>;
}> {
  const { mockJwtGuard = true } = options;
  const prisma = createPrismaMock();

  let moduleBuilder = Test.createTestingModule({
    imports: [AppModule],
  }).overrideProvider(PrismaService).useValue(prisma);

  if (mockJwtGuard) {
    moduleBuilder = moduleBuilder.overrideGuard(JwtAuthGuard).useValue({
      canActivate(context: ExecutionContext) {
        const req = context.switchToHttp().getRequest();
        req.user = { ...TEST_AUTH_USER };
        return true;
      },
    });
  }

  const moduleRef = await moduleBuilder.compile();
  const app = moduleRef.createNestApplication();
  app.setGlobalPrefix('api/v1');
  app.useGlobalPipes(
    new ValidationPipe({ whitelist: true, transform: true, forbidNonWhitelisted: true }),
  );
  await app.init();
  return { app, moduleRef, prisma };
}

export { TEST_PASSWORD };
