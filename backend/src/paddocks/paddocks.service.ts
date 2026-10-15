import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreatePotreroDto, UpdateSpeciesDto } from './paddocks.dto';

@Injectable()
export class PaddocksService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(fincaId: string) {
    return this.prisma.potrero.findMany({ where: { fincaId }, orderBy: { nombre: 'asc' } });
  }

  async findOne(fincaId: string, id: string) {
    const p = await this.prisma.potrero.findFirst({ where: { id, fincaId } });
    if (!p) throw new NotFoundException('Potrero no encontrado');
    return p;
  }

  create(fincaId: string, dto: CreatePotreroDto) {
    return this.prisma.potrero.create({
      data: {
        fincaId,
        nombre: dto.nombre,
        areaHectareas: dto.areaHectareas,
        estado: dto.estado ?? 'DISPONIBLE',
        tipoPasto: dto.tipoPasto,
        fechaUltimaSiembra: dto.fechaUltimaSiembra ? new Date(dto.fechaUltimaSiembra) : null,
        fechaProximoUso: dto.fechaProximoUso ? new Date(dto.fechaProximoUso) : null,
        observacion: dto.observacion ?? '',
        especiesPermitidas: dto.especiesPermitidas ?? [],
      },
    });
  }

  async update(fincaId: string, id: string, dto: CreatePotreroDto) {
    await this.findOne(fincaId, id);
    return this.prisma.potrero.update({
      where: { id },
      data: {
        nombre: dto.nombre,
        areaHectareas: dto.areaHectareas,
        estado: dto.estado,
        tipoPasto: dto.tipoPasto,
        fechaUltimaSiembra: dto.fechaUltimaSiembra ? new Date(dto.fechaUltimaSiembra) : undefined,
        fechaProximoUso: dto.fechaProximoUso ? new Date(dto.fechaProximoUso) : undefined,
        observacion: dto.observacion,
        especiesPermitidas: dto.especiesPermitidas,
        syncVersion: { increment: 1 },
      },
    });
  }

  async updateSpecies(fincaId: string, id: string, dto: UpdateSpeciesDto) {
    await this.findOne(fincaId, id);
    return this.prisma.potrero.update({
      where: { id },
      data: { especiesPermitidas: dto.especiesPermitidas, syncVersion: { increment: 1 } },
    });
  }

  async remove(fincaId: string, id: string) {
    await this.findOne(fincaId, id);
    return this.prisma.potrero.delete({ where: { id } });
  }
}
