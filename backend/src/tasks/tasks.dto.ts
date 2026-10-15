import { TaskType } from '@prisma/client';
import { IsDateString, IsEnum, IsOptional, IsString } from 'class-validator';

export class CreateTaskDto {
  @IsOptional()
  @IsString()
  titulo?: string;

  @IsEnum(TaskType)
  tipo!: TaskType;

  @IsDateString()
  vencimiento!: string;

  @IsOptional()
  @IsString()
  referencia?: string;

  @IsOptional()
  @IsString()
  workType?: string;

  @IsOptional()
  @IsString()
  observacion?: string;
}
