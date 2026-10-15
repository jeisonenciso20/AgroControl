import { DashboardService } from './dashboard.service';

describe('DashboardService', () => {
  it('returns summary counts', async () => {
    const prisma = {
      animal: { count: jest.fn().mockResolvedValue(3) },
      potrero: { count: jest.fn().mockResolvedValue(2) },
      reproductionEvent: { count: jest.fn().mockResolvedValue(1) },
      productionRecord: { aggregate: jest.fn().mockResolvedValue({ _sum: { cantidad: 50 } }) },
      pendingTask: { findMany: jest.fn().mockResolvedValue([]) },
    };
    const service = new DashboardService(prisma as never);
    const result = await service.summary('finca-1');
    expect(result.animalesRegistrados).toBe(3);
    expect(result.litrosLecheSemana).toBe(50);
  });
});
