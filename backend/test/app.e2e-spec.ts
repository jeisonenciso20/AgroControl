import { INestApplication } from '@nestjs/common';
import request from 'supertest';
import { createTestApp } from './helpers/create-test-app';

describe('AgroControl API (e2e)', () => {
  let app: INestApplication;

  beforeAll(async () => {
    ({ app } = await createTestApp({ mockJwtGuard: false }));
  });

  afterAll(async () => {
    await app.close();
  });

  it('GET /animals without token returns 401', () => {
    return request(app.getHttpServer()).get('/api/v1/animals').expect(401);
  });

  it('POST /auth/login validates body', () => {
    return request(app.getHttpServer()).post('/api/v1/auth/login').send({}).expect(400);
  });
});
