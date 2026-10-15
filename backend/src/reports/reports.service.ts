import { Injectable } from '@nestjs/common';
import { PrismaService } from '../prisma/prisma.service';

@Injectable()
export class ReportsService {
  constructor(private readonly prisma: PrismaService) {}

  async weekly(fincaId: string, from: string, to: string) {
    const fromDate = new Date(from);
    const toDate = new Date(to);
    const records = await this.prisma.productionRecord.findMany({
      where: { fincaId, fecha: { gte: fromDate, lte: toDate } },
    });
    const byType = records.reduce<Record<string, number>>((acc, r) => {
      acc[r.productType] = (acc[r.productType] ?? 0) + r.cantidad;
      return acc;
    }, {});
    return { from, to, period: 'weekly', totalsByProductType: byType, recordCount: records.length };
  }

  async monthly(fincaId: string, year: number, month: number) {
    const from = new Date(year, month - 1, 1);
    const to = new Date(year, month, 0);
    return this.weekly(fincaId, from.toISOString().slice(0, 10), to.toISOString().slice(0, 10)).then(
      (r) => ({ ...r, period: 'monthly', year, month }),
    );
  }
}
