import { Injectable } from '@nestjs/common';
import { JornadaOrdeno, ReproEventType, TaskType } from '@prisma/client';
import { AnimalsService } from '../animals/animals.service';
import { PaddocksService } from '../paddocks/paddocks.service';
import { PrismaService } from '../prisma/prisma.service';
import { ProductionService } from '../production/production.service';
import { ReproductionService } from '../reproduction/reproduction.service';
import { TasksService } from '../tasks/tasks.service';
import { SyncChangeDto, SyncEntityDto } from './sync.dto';

@Injectable()
export class SyncService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly animals: AnimalsService,
    private readonly production: ProductionService,
    private readonly tasks: TasksService,
    private readonly paddocks: PaddocksService,
    private readonly reproduction: ReproductionService,
  ) {}

  async push(fincaId: string, changes: SyncChangeDto[]) {
    const results: Array<{ localId: string; serverId: string; entity: string }> = [];
    for (const change of changes) {
      const serverId = await this.applyChange(fincaId, change);
      results.push({ localId: change.localId, serverId, entity: change.entity });
    }
    return { applied: results };
  }

  async pull(fincaId: string, since?: string) {
    const sinceDate = since ? new Date(since) : new Date(0);
    const [animals, production, tasks, potreros, reproductionEvents] = await Promise.all([
      this.prisma.animal.findMany({ where: { fincaId, updatedAt: { gt: sinceDate } } }),
      this.prisma.productionRecord.findMany({ where: { fincaId, updatedAt: { gt: sinceDate } } }),
      this.prisma.pendingTask.findMany({ where: { fincaId, updatedAt: { gt: sinceDate } } }),
      this.prisma.potrero.findMany({ where: { fincaId, updatedAt: { gt: sinceDate } } }),
      this.prisma.reproductionEvent.findMany({
        where: { fincaId, updatedAt: { gt: sinceDate } },
        include: { animal: true },
      }),
    ]);
    return {
      since: sinceDate.toISOString(),
      animals,
      productionRecords: production,
      pendingTasks: tasks,
      potreros,
      reproductionEvents,
    };
  }

  private async applyChange(fincaId: string, change: SyncChangeDto): Promise<string> {
    const p = change.payload;
    switch (change.entity) {
      case SyncEntityDto.ANIMAL: {
        if (change.operation === 'UPDATE' && change.serverId) {
          const updated = await this.animals.update(fincaId, change.serverId, {
            nombre: p.nombre as string,
            tipo: p.tipo as string,
            raza: p.raza as string,
            sexo: p.sexo as string,
            peso: p.peso as number,
            fechaNacimiento: p.fechaNacimiento as string,
            potreroNombre: p.potreroNombre as string,
            observacion: p.observacion as string,
          });
          return updated.id;
        }
        const codigo = p.codigo as string;
        const animalData = {
          nombre: (p.nombre as string) ?? '',
          tipo: p.tipo as string,
          raza: p.raza as string,
          sexo: p.sexo as string,
          peso: p.peso as number,
          fechaNacimiento: new Date(p.fechaNacimiento as string),
          potreroNombre: (p.potreroNombre as string) ?? '',
          observacion: (p.observacion as string) ?? '',
        };
        const animal = await this.prisma.animal.upsert({
          where: { fincaId_codigo: { fincaId, codigo } },
          update: animalData,
          create: { fincaId, codigo, ...animalData },
        });
        return animal.id;
      }
      case SyncEntityDto.PRODUCTION_RECORD: {
        const created = await this.production.create(fincaId, {
          productType: p.productType as never,
          cantidad: p.cantidad as number,
          unidad: p.unidad as string,
          fecha: p.fecha as string,
          jornada: (p.jornada as JornadaOrdeno) ?? JornadaOrdeno.MANANA,
          animalId: p.animalId as string,
          notas: p.notas as string,
        });
        return created.id;
      }
      case SyncEntityDto.PENDING_TASK: {
        if (change.operation === 'UPDATE' && change.serverId) {
          if (p.status === 'COMPLETED') {
            return (await this.tasks.complete(fincaId, change.serverId)).id;
          }
          return change.serverId;
        }
        const created = await this.tasks.create(fincaId, {
          titulo: p.titulo as string,
          tipo: p.tipo as TaskType,
          vencimiento: p.vencimiento as string,
          referencia: p.referencia as string,
          workType: p.workType as string,
          observacion: p.observacion as string,
        });
        return created.id;
      }
      case SyncEntityDto.POTRERO: {
        if (change.operation === 'UPDATE' && change.serverId) {
          const updated = await this.paddocks.update(fincaId, change.serverId, {
            nombre: p.nombre as string,
            areaHectareas: p.areaHectareas as number,
            estado: p.estado as string,
            tipoPasto: p.tipoPasto as string,
            fechaUltimaSiembra: p.fechaUltimaSiembra as string,
            fechaProximoUso: p.fechaProximoUso as string,
            observacion: p.observacion as string,
            especiesPermitidas: p.especiesPermitidas as string[],
          });
          return updated.id;
        }
        const created = await this.paddocks.create(fincaId, {
          nombre: p.nombre as string,
          areaHectareas: p.areaHectareas as number,
          estado: p.estado as string,
          tipoPasto: p.tipoPasto as string,
          fechaUltimaSiembra: p.fechaUltimaSiembra as string,
          fechaProximoUso: p.fechaProximoUso as string,
          observacion: p.observacion as string,
          especiesPermitidas: p.especiesPermitidas as string[],
        });
        return created.id;
      }
      case SyncEntityDto.REPRODUCTION_EVENT: {
        const created = await this.reproduction.create(fincaId, {
          animalId: p.animalId as string,
          tipoEvento: p.tipoEvento as ReproEventType,
          programada: p.programada as string,
          realizada: p.realizada as string,
          notas: p.notas as string,
          datos: p.datos as Record<string, unknown>,
        });
        return created.id;
      }
      default:
        return '';
    }
  }
}
