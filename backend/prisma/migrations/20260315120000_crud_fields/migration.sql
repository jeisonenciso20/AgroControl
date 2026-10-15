-- CreateEnum
CREATE TYPE "JornadaOrdeno" AS ENUM ('MANANA', 'TARDE');
CREATE TYPE "ReproEventType" AS ENUM ('CELO', 'INSEMINACION', 'DIAGNOSTICO', 'PARTO');

-- AlterEnum
ALTER TYPE "SyncEntity" ADD VALUE 'REPRODUCTION_EVENT';

-- Potrero
ALTER TABLE "Potrero" ADD COLUMN "tipoPasto" TEXT;
ALTER TABLE "Potrero" ADD COLUMN "fechaUltimaSiembra" DATE;
ALTER TABLE "Potrero" ADD COLUMN "fechaProximoUso" DATE;
ALTER TABLE "Potrero" ADD COLUMN "observacion" TEXT NOT NULL DEFAULT '';

-- ProductionRecord
ALTER TABLE "ProductionRecord" ADD COLUMN "jornada" "JornadaOrdeno" NOT NULL DEFAULT 'MANANA';
CREATE UNIQUE INDEX "ProductionRecord_fincaId_animalId_fecha_jornada_key" ON "ProductionRecord"("fincaId", "animalId", "fecha", "jornada");

-- PendingTask
ALTER TABLE "PendingTask" ADD COLUMN "workType" TEXT;
ALTER TABLE "PendingTask" ADD COLUMN "observacion" TEXT NOT NULL DEFAULT '';

-- ReproductionEvent: migrate tipoEvento to enum
ALTER TABLE "ReproductionEvent" ADD COLUMN "datosJson" JSONB NOT NULL DEFAULT '{}';
ALTER TABLE "ReproductionEvent" ADD COLUMN "tipoEvento_new" "ReproEventType";

UPDATE "ReproductionEvent" SET "tipoEvento_new" = CASE
  WHEN LOWER("tipoEvento") LIKE '%celo%' THEN 'CELO'::"ReproEventType"
  WHEN LOWER("tipoEvento") LIKE '%insemin%' THEN 'INSEMINACION'::"ReproEventType"
  WHEN LOWER("tipoEvento") LIKE '%parto%' THEN 'PARTO'::"ReproEventType"
  ELSE 'DIAGNOSTICO'::"ReproEventType"
END;

ALTER TABLE "ReproductionEvent" DROP COLUMN "tipoEvento";
ALTER TABLE "ReproductionEvent" RENAME COLUMN "tipoEvento_new" TO "tipoEvento";
ALTER TABLE "ReproductionEvent" ALTER COLUMN "tipoEvento" SET NOT NULL;
