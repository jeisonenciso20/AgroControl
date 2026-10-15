import { Body, Controller, Delete, Get, Param, Patch, Post, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { AuthUser, CurrentUser } from '../common/current-user.decorator';
import { CreateAnimalDto, UpdateAnimalDto } from './animals.dto';
import { AnimalsService } from './animals.service';

@ApiTags('animals')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('animals')
export class AnimalsController {
  constructor(private readonly animalsService: AnimalsService) {}

  @Get()
  findAll(@CurrentUser() user: AuthUser, @Query('q') q?: string) {
    return this.animalsService.findAll(user.fincaId, q);
  }

  @Get(':id')
  findOne(@CurrentUser() user: AuthUser, @Param('id') id: string) {
    return this.animalsService.findOne(user.fincaId, id);
  }

  @Post()
  create(@CurrentUser() user: AuthUser, @Body() dto: CreateAnimalDto) {
    return this.animalsService.create(user.fincaId, dto);
  }

  @Patch(':id')
  update(@CurrentUser() user: AuthUser, @Param('id') id: string, @Body() dto: UpdateAnimalDto) {
    return this.animalsService.update(user.fincaId, id, dto);
  }

  @Delete(':id')
  remove(@CurrentUser() user: AuthUser, @Param('id') id: string) {
    return this.animalsService.remove(user.fincaId, id);
  }
}
