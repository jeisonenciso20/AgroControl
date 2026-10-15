import { Injectable, NotFoundException } from '@nestjs/common';
import { TaskStatus } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { CreateTaskDto } from './tasks.dto';

function buildTitulo(dto: CreateTaskDto): string {
  if (dto.titulo?.trim()) return dto.titulo.trim();
  const work = dto.workType ?? 'Trabajo';
  const ref = dto.referencia?.trim() ?? '';
  return ref ? `${work} ${ref}` : work;
}

@Injectable()
export class TasksService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(fincaId: string, status?: TaskStatus) {
    return this.prisma.pendingTask.findMany({
      where: { fincaId, ...(status ? { status } : {}) },
      orderBy: { vencimiento: 'asc' },
    });
  }

  async findOne(fincaId: string, id: string) {
    const t = await this.prisma.pendingTask.findFirst({ where: { id, fincaId } });
    if (!t) throw new NotFoundException('Tarea no encontrada');
    return t;
  }

  create(fincaId: string, dto: CreateTaskDto) {
    return this.prisma.pendingTask.create({
      data: {
        fincaId,
        titulo: buildTitulo(dto),
        tipo: dto.tipo,
        vencimiento: new Date(dto.vencimiento),
        referencia: dto.referencia ?? '',
        workType: dto.workType,
        observacion: dto.observacion ?? '',
      },
    });
  }

  async complete(fincaId: string, id: string) {
    await this.findOne(fincaId, id);
    return this.prisma.pendingTask.update({
      where: { id },
      data: { status: TaskStatus.COMPLETED, syncVersion: { increment: 1 } },
    });
  }

  async remove(fincaId: string, id: string) {
    await this.findOne(fincaId, id);
    return this.prisma.pendingTask.delete({ where: { id } });
  }
}
