import { Injectable, NotFoundException } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';
import { CreateAnimalDto, UpdateAnimalDto } from './animals.dto';

@Injectable()
export class AnimalsService {
  constructor(private readonly prisma: PrismaService) {}

  findAll(fincaId: string, q?: string) {
    return this.prisma.animal.findMany({
      where: {
        fincaId,
        ...(q
          ? {
              OR: [
                { codigo: { contains: q, mode: 'insensitive' } },
                { nombre: { contains: q, mode: 'insensitive' } },
                { tipo: { contains: q, mode: 'insensitive' } },
              ],
            }
          : {}),
      },
      orderBy: { codigo: 'asc' },
    });
  }

  async findOne(fincaId: string, id: string) {
    const animal = await this.prisma.animal.findFirst({ where: { id, fincaId } });
    if (!animal) throw new NotFoundException('Animal no encontrado');
    return animal;
  }

  create(fincaId: string, dto: CreateAnimalDto) {
    return this.prisma.animal.create({
      data: {
        fincaId,
        codigo: dto.codigo,
        nombre: dto.nombre ?? '',
        tipo: dto.tipo,
        raza: dto.raza,
        sexo: dto.sexo,
        peso: dto.peso,
        fechaNacimiento: new Date(dto.fechaNacimiento),
        potreroNombre: dto.potreroNombre ?? '',
        observacion: dto.observacion ?? '',
      },
    });
  }

  async update(fincaId: string, id: string, dto: UpdateAnimalDto) {
    await this.findOne(fincaId, id);
    return this.prisma.animal.update({
      where: { id },
      data: {
        ...dto,
        fechaNacimiento: dto.fechaNacimiento ? new Date(dto.fechaNacimiento) : undefined,
        syncVersion: { increment: 1 },
      },
    });
  }

  async remove(fincaId: string, id: string) {
    await this.findOne(fincaId, id);
    return this.prisma.animal.delete({ where: { id } });
  }
}
