import { Injectable } from '@nestjs/common';
import { ProductType, TaskStatus } from '@prisma/client';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class DashboardService {
  constructor(private readonly prisma: PrismaService) {}

  async summary(fincaId: string) {
    const [animales, potreros, reproduccion, lecheAgg, tareas] = await Promise.all([
      this.prisma.animal.count({ where: { fincaId } }),
      this.prisma.potrero.count({ where: { fincaId } }),
      this.prisma.reproductionEvent.count({
        where: { fincaId, realizada: null },
      }),
      this.prisma.productionRecord.aggregate({
        where: {
          fincaId,
          productType: ProductType.LECHE,
          fecha: { gte: new Date(new Date().setDate(new Date().getDate() - 7)) },
        },
        _sum: { cantidad: true },
      }),
      this.prisma.pendingTask.findMany({
        where: { fincaId, status: TaskStatus.PENDING },
        orderBy: { vencimiento: 'asc' },
        take: 5,
      }),
    ]);

    return {
      animalesRegistrados: animales,
      potrerosRegistrados: potreros,
      eventosReproduccion: reproduccion,
      litrosLecheSemana: lecheAgg._sum.cantidad ?? 0,
      pendientesRecientes: tareas,
    };
  }
}
