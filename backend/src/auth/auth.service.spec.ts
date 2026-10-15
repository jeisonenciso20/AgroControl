import { ConflictException, UnauthorizedException } from '@nestjs/common';
import { JwtService } from '@nestjs/jwt';
import { ConfigService } from '@nestjs/config';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  const prisma = {
    user: {
      findUnique: jest.fn(),
      create: jest.fn(),
    },
    finca: { create: jest.fn() },
  };
  const jwt = { sign: jest.fn().mockReturnValue('token'), verify: jest.fn() };
  const config = { get: jest.fn().mockReturnValue('secret') };
  let service: AuthService;

  beforeEach(() => {
    jest.clearAllMocks();
    service = new AuthService(prisma as never, jwt as unknown as JwtService, config as unknown as ConfigService);
  });

  it('login fails with bad credentials', async () => {
    prisma.user.findUnique.mockResolvedValue(null);
    await expect(service.login({ email: 'a@b.com', password: 'x' })).rejects.toBeInstanceOf(
      UnauthorizedException,
    );
  });

  it('register fails if email exists', async () => {
    prisma.user.findUnique.mockResolvedValue({ id: '1' });
    await expect(
      service.register({
        email: 'a@b.com',
        password: 'password1',
        nombre: 'N',
        fincaNombre: 'F',
      }),
    ).rejects.toBeInstanceOf(ConflictException);
  });
});
