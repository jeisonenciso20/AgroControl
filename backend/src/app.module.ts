import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { ThrottlerModule } from '@nestjs/throttler';
import { AnimalsModule } from './animals/animals.module';
import { AuthModule } from './auth/auth.module';
import { DashboardModule } from './dashboard/dashboard.module';
import { PaddocksModule } from './paddocks/paddocks.module';
import { PrismaModule } from './prisma/prisma.module';
import { ProductionModule } from './production/production.module';
import { ReportsModule } from './reports/reports.module';
import { ReproductionModule } from './reproduction/reproduction.module';
import { SyncModule } from './sync/sync.module';
import { TasksModule } from './tasks/tasks.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    ThrottlerModule.forRoot([{ ttl: 60_000, limit: 10 }]),
    PrismaModule,
    AuthModule,
    AnimalsModule,
    PaddocksModule,
    ProductionModule,
    ReproductionModule,
    TasksModule,
    ReportsModule,
    DashboardModule,
    SyncModule,
  ],
})
export class AppModule {}
