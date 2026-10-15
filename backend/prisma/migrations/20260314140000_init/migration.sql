-- CreateSchema
CREATE SCHEMA IF NOT EXISTS "public";

-- CreateEnum
CREATE TYPE "ProductType" AS ENUM ('LECHE', 'CARNE', 'HUEVO', 'OTRO');

-- CreateEnum
CREATE TYPE "TaskType" AS ENUM ('POTRERO', 'ANIMAL', 'GENERAL');

-- CreateEnum
CREATE TYPE "TaskStatus" AS ENUM ('PENDING', 'COMPLETED');

-- CreateEnum
CREATE TYPE "SyncEntity" AS ENUM ('ANIMAL', 'PRODUCTION_RECORD', 'PENDING_TASK', 'POTRERO');

-- CreateTable
CREATE TABLE "User" (
    "id" TEXT NOT NULL,
    "email" TEXT NOT NULL,
    "passwordHash" TEXT NOT NULL,
    "nombre" TEXT NOT NULL,
    "fincaId" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "User_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Finca" (
    "id" TEXT NOT NULL,
    "nombre" TEXT NOT NULL,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Finca_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Animal" (
    "id" TEXT NOT NULL,
    "fincaId" TEXT NOT NULL,
    "codigo" TEXT NOT NULL,
    "nombre" TEXT NOT NULL DEFAULT '',
    "tipo" TEXT NOT NULL,
    "raza" TEXT NOT NULL,
    "sexo" TEXT NOT NULL,
    "peso" DOUBLE PRECISION NOT NULL,
    "fechaNacimiento" DATE NOT NULL,
    "potreroNombre" TEXT NOT NULL DEFAULT '',
    "observacion" TEXT NOT NULL DEFAULT '',
    "syncVersion" INTEGER NOT NULL DEFAULT 1,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Animal_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Potrero" (
    "id" TEXT NOT NULL,
    "fincaId" TEXT NOT NULL,
    "nombre" TEXT NOT NULL,
    "areaHectareas" DOUBLE PRECISION,
    "estado" TEXT NOT NULL DEFAULT 'ACTIVO',
    "especiesPermitidas" JSONB NOT NULL DEFAULT '[]',
    "syncVersion" INTEGER NOT NULL DEFAULT 1,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Potrero_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "AnimalPotrero" (
    "id" TEXT NOT NULL,
    "animalId" TEXT NOT NULL,
    "potreroId" TEXT NOT NULL,
    "desde" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "hasta" TIMESTAMP(3),

    CONSTRAINT "AnimalPotrero_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ProductionRecord" (
    "id" TEXT NOT NULL,
    "fincaId" TEXT NOT NULL,
    "animalId" TEXT,
    "productType" "ProductType" NOT NULL,
    "cantidad" DOUBLE PRECISION NOT NULL,
    "unidad" TEXT NOT NULL,
    "fecha" DATE NOT NULL,
    "notas" TEXT NOT NULL DEFAULT '',
    "syncVersion" INTEGER NOT NULL DEFAULT 1,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "ProductionRecord_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ReproductionEvent" (
    "id" TEXT NOT NULL,
    "fincaId" TEXT NOT NULL,
    "animalId" TEXT NOT NULL,
    "tipoEvento" TEXT NOT NULL,
    "programada" DATE NOT NULL,
    "realizada" DATE,
    "notas" TEXT NOT NULL DEFAULT '',
    "syncVersion" INTEGER NOT NULL DEFAULT 1,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "ReproductionEvent_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "PendingTask" (
    "id" TEXT NOT NULL,
    "fincaId" TEXT NOT NULL,
    "titulo" TEXT NOT NULL,
    "tipo" "TaskType" NOT NULL,
    "status" "TaskStatus" NOT NULL DEFAULT 'PENDING',
    "vencimiento" DATE NOT NULL,
    "referencia" TEXT NOT NULL DEFAULT '',
    "syncVersion" INTEGER NOT NULL DEFAULT 1,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "PendingTask_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "User_email_key" ON "User"("email");

-- CreateIndex
CREATE INDEX "Animal_fincaId_updatedAt_idx" ON "Animal"("fincaId", "updatedAt");

-- CreateIndex
CREATE UNIQUE INDEX "Animal_fincaId_codigo_key" ON "Animal"("fincaId", "codigo");

-- CreateIndex
CREATE UNIQUE INDEX "Potrero_fincaId_nombre_key" ON "Potrero"("fincaId", "nombre");

-- CreateIndex
CREATE INDEX "ProductionRecord_fincaId_fecha_idx" ON "ProductionRecord"("fincaId", "fecha");

-- CreateIndex
CREATE INDEX "PendingTask_fincaId_status_vencimiento_idx" ON "PendingTask"("fincaId", "status", "vencimiento");

-- AddForeignKey
ALTER TABLE "User" ADD CONSTRAINT "User_fincaId_fkey" FOREIGN KEY ("fincaId") REFERENCES "Finca"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Animal" ADD CONSTRAINT "Animal_fincaId_fkey" FOREIGN KEY ("fincaId") REFERENCES "Finca"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Potrero" ADD CONSTRAINT "Potrero_fincaId_fkey" FOREIGN KEY ("fincaId") REFERENCES "Finca"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "AnimalPotrero" ADD CONSTRAINT "AnimalPotrero_animalId_fkey" FOREIGN KEY ("animalId") REFERENCES "Animal"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "AnimalPotrero" ADD CONSTRAINT "AnimalPotrero_potreroId_fkey" FOREIGN KEY ("potreroId") REFERENCES "Potrero"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ProductionRecord" ADD CONSTRAINT "ProductionRecord_fincaId_fkey" FOREIGN KEY ("fincaId") REFERENCES "Finca"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ProductionRecord" ADD CONSTRAINT "ProductionRecord_animalId_fkey" FOREIGN KEY ("animalId") REFERENCES "Animal"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ReproductionEvent" ADD CONSTRAINT "ReproductionEvent_fincaId_fkey" FOREIGN KEY ("fincaId") REFERENCES "Finca"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ReproductionEvent" ADD CONSTRAINT "ReproductionEvent_animalId_fkey" FOREIGN KEY ("animalId") REFERENCES "Animal"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "PendingTask" ADD CONSTRAINT "PendingTask_fincaId_fkey" FOREIGN KEY ("fincaId") REFERENCES "Finca"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
