import { IsArray, IsDateString, IsNumber, IsOptional, IsString, Min } from 'class-validator';

export class CreatePotreroDto {
  @IsString()
  nombre!: string;

  @IsOptional()
  @IsNumber()
  @Min(0.01)
  areaHectareas?: number;

  @IsOptional()
  @IsString()
  estado?: string;

  @IsOptional()
  @IsString()
  tipoPasto?: string;

  @IsOptional()
  @IsDateString()
  fechaUltimaSiembra?: string;

  @IsOptional()
  @IsDateString()
  fechaProximoUso?: string;

  @IsOptional()
  @IsString()
  observacion?: string;

  @IsOptional()
  @IsArray()
  especiesPermitidas?: string[];
}

export class UpdateSpeciesDto {
  @IsArray()
  especiesPermitidas!: string[];
}
