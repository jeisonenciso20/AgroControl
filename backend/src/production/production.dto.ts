import { JornadaOrdeno, ProductType } from '@prisma/client';
import { IsDateString, IsEnum, IsNumber, IsOptional, IsString, Min } from 'class-validator';

export class CreateProductionDto {
  @IsEnum(ProductType)
  productType!: ProductType;

  @IsNumber()
  @Min(0.01)
  cantidad!: number;

  @IsString()
  unidad!: string;

  @IsDateString()
  fecha!: string;

  @IsEnum(JornadaOrdeno)
  jornada!: JornadaOrdeno;

  @IsOptional()
  @IsString()
  animalId?: string;

  @IsOptional()
  @IsString()
  notas?: string;
}
