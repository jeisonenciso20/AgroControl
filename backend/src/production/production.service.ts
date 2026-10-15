import { Injectable, NotFoundException } from '@nestjs/common';
import { ProductType } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';
import { CreateProductionDto } from './production.dto';

@Injectable()
export class ProductionService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(fincaId: string, from?: string, to?: string, productType?: ProductType) {
    return this.prisma.productionRecord.findMany({
      where: {
        fincaId,
        ...(productType ? { productType } : {}),
        ...(from || to
          ? {
              fecha: {
                ...(from ? { gte: new Date(from) } : {}),
                ...(to ? { lte: new Date(to) } : {}),
              },
            }
          : {}),
      },
      orderBy: { fecha: 'desc' },
      include: { animal: true },
    });
  }

  async findOne(fincaId: string, id: string) {
    const r = await this.prisma.productionRecord.findFirst({ where: { id, fincaId } });
    if (!r) throw new NotFoundException('Registro no encontrado');
    return r;
  }

  create(fincaId: string, dto: CreateProductionDto) {
    return this.prisma.productionRecord.create({
      data: {
        fincaId,
        productType: dto.productType,
        cantidad: dto.cantidad,
        unidad: dto.unidad,
        fecha: new Date(dto.fecha),
        jornada: dto.jornada,
        animalId: dto.animalId,
        notas: dto.notas ?? '',
      },
    });
  }

  async update(fincaId: string, id: string, dto: Partial<CreateProductionDto>) {
    await this.findOne(fincaId, id);
    return this.prisma.productionRecord.update({
      where: { id },
      data: {
        productType: dto.productType,
        cantidad: dto.cantidad,
        unidad: dto.unidad,
        fecha: dto.fecha ? new Date(dto.fecha) : undefined,
        jornada: dto.jornada,
        animalId: dto.animalId,
        notas: dto.notas,
        syncVersion: { increment: 1 },
      },
    });
  }

  async remove(fincaId: string, id: string) {
    await this.findOne(fincaId, id);
    return this.prisma.productionRecord.delete({ where: { id } });
  }
}
