import { Injectable, NotFoundException } from '@nestjs/common';
import { Prisma, ReproEventType } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { CreateReproductionDto } from './reproduction.dto';

@Injectable()
export class ReproductionService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(fincaId: string) {
    return this.prisma.reproductionEvent.findMany({
      where: { fincaId },
      orderBy: { programada: 'desc' },
      include: { animal: true },
    });
  }

  async findOne(fincaId: string, id: string) {
    const e = await this.prisma.reproductionEvent.findFirst({ where: { id, fincaId } });
    if (!e) throw new NotFoundException('Evento no encontrado');
    return e;
  }

  async summary(fincaId: string) {
    const events = await this.prisma.reproductionEvent.findMany({
      where: { fincaId },
      orderBy: { programada: 'desc' },
    });
    const now = new Date();
    const threeDaysAgo = new Date(now);
    threeDaysAgo.setDate(threeDaysAgo.getDate() - 3);
    const monthStart = new Date(now.getFullYear(), now.getMonth(), 1);

    const byAnimal = new Map<string, typeof events>();
    for (const e of events) {
      const list = byAnimal.get(e.animalId) ?? [];
      list.push(e);
      byAnimal.set(e.animalId, list);
    }

    let enCelo = 0;
    let inseminadas = 0;
    let gestantes = 0;
    let partosMes = 0;

    for (const e of events) {
      if (e.tipoEvento === ReproEventType.CELO && e.programada >= threeDaysAgo) enCelo++;
      if (e.tipoEvento === ReproEventType.PARTO && e.programada >= monthStart) partosMes++;
    }

    for (const [, animalEvents] of byAnimal) {
      const latest = animalEvents[0];
      if (latest.tipoEvento === ReproEventType.INSEMINACION) inseminadas++;
      else if (latest.tipoEvento === ReproEventType.DIAGNOSTICO) gestantes++;
    }

    return { enCelo, inseminadas, gestantes, partosMes };
  }

  create(fincaId: string, dto: CreateReproductionDto) {
    return this.prisma.reproductionEvent.create({
      data: {
        fincaId,
        animalId: dto.animalId,
        tipoEvento: dto.tipoEvento,
        programada: new Date(dto.programada),
        realizada: dto.realizada ? new Date(dto.realizada) : null,
        notas: dto.notas ?? '',
        datosJson: (dto.datos ?? {}) as Prisma.InputJsonValue,
      },
    });
  }

  async update(fincaId: string, id: string, dto: Partial<CreateReproductionDto>) {
    await this.findOne(fincaId, id);
    return this.prisma.reproductionEvent.update({
      where: { id },
      data: {
        tipoEvento: dto.tipoEvento,
        programada: dto.programada ? new Date(dto.programada) : undefined,
        realizada: dto.realizada ? new Date(dto.realizada) : undefined,
        notas: dto.notas,
        datosJson: dto.datos as Prisma.InputJsonValue | undefined,
        syncVersion: { increment: 1 },
      },
    });
  }

  async remove(fincaId: string, id: string) {
    await this.findOne(fincaId, id);
    return this.prisma.reproductionEvent.delete({ where: { id } });
  }
}
