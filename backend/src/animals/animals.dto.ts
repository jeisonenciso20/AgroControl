import { IsDateString, IsNumber, IsOptional, IsString, Min } from 'class-validator';

export class CreateAnimalDto {
  @IsString()
  codigo!: string;

  @IsOptional()
  @IsString()
  nombre?: string;

  @IsString()
  tipo!: string;

  @IsString()
  raza!: string;

  @IsString()
  sexo!: string;

  @IsNumber()
  @Min(0.01)
  peso!: number;

  @IsDateString()
  fechaNacimiento!: string;

  @IsOptional()
  @IsString()
  potreroNombre?: string;

  @IsOptional()
  @IsString()
  observacion?: string;
}

export class UpdateAnimalDto {
  @IsOptional()
  @IsString()
  nombre?: string;

  @IsOptional()
  @IsString()
  tipo?: string;

  @IsOptional()
  @IsString()
  raza?: string;

  @IsOptional()
  @IsString()
  sexo?: string;

  @IsOptional()
  @IsNumber()
  peso?: number;

  @IsOptional()
  @IsDateString()
  fechaNacimiento?: string;

  @IsOptional()
  @IsString()
  potreroNombre?: string;

  @IsOptional()
  @IsString()
  observacion?: string;
}
