import { Body, Controller, Get, Post, Query, UseGuards } from '@nestjs/common';
import { ApiBearerAuth, ApiTags } from '@nestjs/swagger';
import { JwtAuthGuard } from '../auth/jwt-auth.guard';
import { AuthUser, CurrentUser } from '../common/current-user.decorator';
import { SyncPushDto } from './sync.dto';
import { SyncService } from './sync.service';

@ApiTags('sync')
@ApiBearerAuth()
@UseGuards(JwtAuthGuard)
@Controller('sync')
export class SyncController {
  constructor(private readonly syncService: SyncService) {}

  @Post('push')
  push(@CurrentUser() user: AuthUser, @Body() dto: SyncPushDto) {
    return this.syncService.push(user.fincaId, dto.changes);
  }

  @Get('pull')
  pull(@CurrentUser() user: AuthUser, @Query('since') since?: string) {
    return this.syncService.pull(user.fincaId, since);
  }
}
