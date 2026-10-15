import { ReproEventType } from '@prisma/client';
import { IsDateString, IsEnum, IsObject, IsOptional, IsString } from 'class-validator';

export class CreateReproductionDto {
  @IsString()
  animalId!: string;

  @IsEnum(ReproEventType)
  tipoEvento!: ReproEventType;

  @IsDateString()
  programada!: string;

  @IsOptional()
  @IsDateString()
  realizada?: string;

  @IsOptional()
  @IsString()
  notas?: string;

  @IsOptional()
  @IsObject()
  datos?: Record<string, unknown>;
}
