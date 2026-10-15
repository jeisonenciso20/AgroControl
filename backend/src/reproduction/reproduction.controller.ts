import { Body, Controller, Delete, Get, Param, Patch, Post, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { AuthUser, CurrentUser } from '../common/current-user.decorator';
import { CreateReproductionDto } from './reproduction.dto';
import { ReproductionService } from './reproduction.service';

@ApiTags('reproduction')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('reproduction')
export class ReproductionController {
  constructor(private readonly reproductionService: ReproductionService) {}

  @Get('summary')
  summary(@CurrentUser() user: AuthUser) {
    return this.reproductionService.summary(user.fincaId);
  }

  @Get()
  findAll(@CurrentUser() user: AuthUser) {
    return this.reproductionService.findAll(user.fincaId);
  }

  @Post()
  create(@CurrentUser() user: AuthUser, @Body() dto: CreateReproductionDto) {
    return this.reproductionService.create(user.fincaId, dto);
  }

  @Patch(':id')
  update(@CurrentUser() user: AuthUser, @Param('id') id: string, @Body() dto: CreateReproductionDto) {
    return this.reproductionService.update(user.fincaId, id, dto);
  }

  @Delete(':id')
  remove(@CurrentUser() user: AuthUser, @Param('id') id: string) {
    return this.reproductionService.remove(user.fincaId, id);
  }
}
