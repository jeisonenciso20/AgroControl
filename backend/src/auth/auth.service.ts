import { ConflictException, Injectable, UnauthorizedException } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { JwtService } from '@nestjs/jwt';
import * as bcrypt from 'bcrypt';
import { PrismaService } from '../prisma/prisma.service';
import { LoginDto, RegisterDto } from './auth.dto';

@Injectable()
export class AuthService {
  constructor(
    private readonly prisma: PrismaService,
    private readonly jwt: JwtService,
    private readonly config: ConfigService,
  ) {}

  async register(dto: RegisterDto) {
    const existing = await this.prisma.user.findUnique({ where: { email: dto.email } });
    if (existing) throw new ConflictException('Email ya registrado');

    const finca = await this.prisma.finca.create({ data: { nombre: dto.fincaNombre } });
    const passwordHash = await bcrypt.hash(dto.password, 10);
    const user = await this.prisma.user.create({
      data: {
        email: dto.email,
        passwordHash,
        nombre: dto.nombre,
        fincaId: finca.id,
      },
    });
    return this.issueTokens(user.id, user.email, user.fincaId);
  }

  async login(dto: LoginDto) {
    const user = await this.prisma.user.findUnique({ where: { email: dto.email } });
    if (!user) throw new UnauthorizedException('Credenciales inválidas');
    const ok = await bcrypt.compare(dto.password, user.passwordHash);
    if (!ok) throw new UnauthorizedException('Credenciales inválidas');
    return this.issueTokens(user.id, user.email, user.fincaId);
  }

  async refresh(refreshToken: string) {
    try {
      const payload = this.jwt.verify<{ sub: string; type: string }>(refreshToken, {
        secret: this.config.get('JWT_REFRESH_SECRET') ?? 'dev-refresh-secret-change-me-32chars',
      });
      if (payload.type !== 'refresh') throw new UnauthorizedException();
      const user = await this.prisma.user.findUnique({ where: { id: payload.sub } });
      if (!user) throw new UnauthorizedException();
      return this.issueTokens(user.id, user.email, user.fincaId);
    } catch {
      throw new UnauthorizedException('Refresh token inválido');
    }
  }

  async me(userId: string) {
    const user = await this.prisma.user.findUnique({
      where: { id: userId },
      include: { finca: true },
    });
    if (!user) throw new UnauthorizedException();
    return {
      id: user.id,
      email: user.email,
      nombre: user.nombre,
      finca: { id: user.finca.id, nombre: user.finca.nombre },
    };
  }

  private issueTokens(userId: string, email: string, fincaId: string) {
    const accessSecret = this.config.get('JWT_ACCESS_SECRET') ?? 'dev-access-secret-change-me-32chars';
    const refreshSecret = this.config.get('JWT_REFRESH_SECRET') ?? 'dev-refresh-secret-change-me-32chars';
    const accessToken = this.jwt.sign(
      { sub: userId, email, fincaId },
      { secret: accessSecret, expiresIn: this.config.get('JWT_ACCESS_EXPIRES') ?? '15m' },
    );
    const refreshToken = this.jwt.sign(
      { sub: userId, type: 'refresh' },
      { secret: refreshSecret, expiresIn: this.config.get('JWT_REFRESH_EXPIRES') ?? '7d' },
    );
    return { accessToken, refreshToken, tokenType: 'Bearer' };
  }
}
