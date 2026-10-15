import { Body, Controller, Delete, Get, Param, Patch, Post, Put, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { AuthUser, CurrentUser } from '../common/current-user.decorator';
import { CreatePotreroDto, UpdateSpeciesDto } from './paddocks.dto';
import { PaddocksService } from './paddocks.service';

@ApiTags('paddocks')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('paddocks')
export class PaddocksController {
  constructor(private readonly paddocksService: PaddocksService) {}

  @Get()
  findAll(@CurrentUser() user: AuthUser) {
    return this.paddocksService.findAll(user.fincaId);
  }

  @Get(':id')
  findOne(@CurrentUser() user: AuthUser, @Param('id') id: string) {
    return this.paddocksService.findOne(user.fincaId, id);
  }

  @Post()
  create(@CurrentUser() user: AuthUser, @Body() dto: CreatePotreroDto) {
    return this.paddocksService.create(user.fincaId, dto);
  }

  @Patch(':id')
  update(@CurrentUser() user: AuthUser, @Param('id') id: string, @Body() dto: CreatePotreroDto) {
    return this.paddocksService.update(user.fincaId, id, dto);
  }

  @Put(':id/species')
  updateSpecies(@CurrentUser() user: AuthUser, @Param('id') id: string, @Body() dto: UpdateSpeciesDto) {
    return this.paddocksService.updateSpecies(user.fincaId, id, dto);
  }

  @Delete(':id')
  remove(@CurrentUser() user: AuthUser, @Param('id') id: string) {
    return this.paddocksService.remove(user.fincaId, id);
  }
}
