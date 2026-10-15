import { Module } from '@nestjs/common';
import { AnimalsModule } from '../animals/animals.module';
import { PaddocksModule } from '../paddocks/paddocks.module';
import { ProductionModule } from '../production/production.module';
import { ReproductionModule } from '../reproduction/reproduction.module';
import { TasksModule } from '../tasks/tasks.module';
import { SyncController } from './sync.controller';
import { SyncService } from './sync.service';

@Module({
  imports: [AnimalsModule, ProductionModule, TasksModule, PaddocksModule, ReproductionModule],
  controllers: [SyncController],
  providers: [SyncService],
})
export class SyncModule {}
