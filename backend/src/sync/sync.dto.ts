import { Type } from 'class-transformer';
import { IsArray, IsDateString, IsEnum, IsObject, IsOptional, IsString, ValidateNested } from 'class-validator';

export enum SyncEntityDto {
  ANIMAL = 'ANIMAL',
  PRODUCTION_RECORD = 'PRODUCTION_RECORD',
  PENDING_TASK = 'PENDING_TASK',
  POTRERO = 'POTRERO',
  REPRODUCTION_EVENT = 'REPRODUCTION_EVENT',
}

export class SyncChangeDto {
  @IsEnum(SyncEntityDto)
  entity!: SyncEntityDto;

  @IsString()
  operation!: 'CREATE' | 'UPDATE';

  @IsOptional()
  @IsString()
  serverId?: string;

  @IsString()
  localId!: string;

  @IsObject()
  payload!: Record<string, unknown>;

  @IsDateString()
  clientUpdatedAt!: string;
}

export class SyncPushDto {
  @IsArray()
  @ValidateNested({ each: true })
  @Type(() => SyncChangeDto)
  changes!: SyncChangeDto[];
}
