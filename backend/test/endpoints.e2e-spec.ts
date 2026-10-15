import { INestApplication } from '@nestjs/common';
import request from 'supertest';
import {
  TEST_ENTITY_ID,
  TEST_PASSWORD,
  createTestApp,
} from './helpers/create-test-app';

describe('API endpoints (e2e, mocked)', () => {
  let app: INestApplication;

  beforeAll(async () => {
    ({ app } = await createTestApp({ mockJwtGuard: true }));
  });

  afterAll(async () => {
    await app.close();
  });

  describe('auth', () => {
    it('POST /auth/register', () => {
      return request(app.getHttpServer())
        .post('/api/v1/auth/register')
        .send({
          email: 'newuser@agrocontrol.local',
          password: 'password123',
          nombre: 'Nuevo',
          fincaNombre: 'Mi Finca',
        })
        .expect(201)
        .expect((res) => {
          expect(res.body.accessToken).toBeDefined();
        });
    });

    it('POST /auth/login', () => {
      return request(app.getHttpServer())
        .post('/api/v1/auth/login')
        .send({ email: 'test@agrocontrol.local', password: TEST_PASSWORD })
        .expect(201)
        .expect((res) => {
          expect(res.body.accessToken).toBeDefined();
          expect(res.body.refreshToken).toBeDefined();
        });
    });

    it('POST /auth/refresh', async () => {
      const login = await request(app.getHttpServer())
        .post('/api/v1/auth/login')
        .send({ email: 'test@agrocontrol.local', password: TEST_PASSWORD });
      return request(app.getHttpServer())
        .post('/api/v1/auth/refresh')
        .send({ refreshToken: login.body.refreshToken })
        .expect(201)
        .expect((res) => {
          expect(res.body.accessToken).toBeDefined();
        });
    });

    it('GET /auth/me', () => {
      return request(app.getHttpServer())
        .get('/api/v1/auth/me')
        .expect(200)
        .expect((res) => {
          expect(res.body.email).toBe('test@agrocontrol.local');
          expect(res.body.finca).toBeDefined();
          expect(res.body.finca.id).toBeDefined();
        });
    });
  });

  describe('dashboard', () => {
    it('GET /dashboard/summary', () => {
      return request(app.getHttpServer())
        .get('/api/v1/dashboard/summary')
        .expect(200)
        .expect((res) => {
          expect(res.body.animalesRegistrados).toBeDefined();
        });
    });
  });

  describe('animals', () => {
    it('GET /animals', () => {
      return request(app.getHttpServer()).get('/api/v1/animals').expect(200).expect([]);
    });

    it('GET /animals/:id', () => {
      return request(app.getHttpServer()).get(`/api/v1/animals/${TEST_ENTITY_ID}`).expect(200);
    });

    it('POST /animals', () => {
      return request(app.getHttpServer())
        .post('/api/v1/animals')
        .send({
          codigo: 'V002',
          tipo: 'Vaca',
          raza: 'Jersey',
          sexo: 'Hembra',
          peso: 400,
          fechaNacimiento: '2021-05-01',
        })
        .expect(201);
    });

    it('PATCH /animals/:id', () => {
      return request(app.getHttpServer())
        .patch(`/api/v1/animals/${TEST_ENTITY_ID}`)
        .send({ nombre: 'Actualizado' })
        .expect(200);
    });

    it('DELETE /animals/:id', () => {
      return request(app.getHttpServer()).delete(`/api/v1/animals/${TEST_ENTITY_ID}`).expect(200);
    });
  });

  describe('paddocks', () => {
    it('GET /paddocks', () => {
      return request(app.getHttpServer()).get('/api/v1/paddocks').expect(200).expect([]);
    });

    it('GET /paddocks/:id', () => {
      return request(app.getHttpServer()).get(`/api/v1/paddocks/${TEST_ENTITY_ID}`).expect(200);
    });

    it('POST /paddocks', () => {
      return request(app.getHttpServer())
        .post('/api/v1/paddocks')
        .send({ nombre: 'Potrero Norte', areaHectareas: 3 })
        .expect(201);
    });

    it('PATCH /paddocks/:id', () => {
      return request(app.getHttpServer())
        .patch(`/api/v1/paddocks/${TEST_ENTITY_ID}`)
        .send({ nombre: 'Potrero Sur' })
        .expect(200);
    });

    it('PUT /paddocks/:id/species', () => {
      return request(app.getHttpServer())
        .put(`/api/v1/paddocks/${TEST_ENTITY_ID}/species`)
        .send({ especiesPermitidas: ['Vaca', 'Novillo'] })
        .expect(200);
    });

    it('DELETE /paddocks/:id', () => {
      return request(app.getHttpServer()).delete(`/api/v1/paddocks/${TEST_ENTITY_ID}`).expect(200);
    });
  });

  describe('production', () => {
    it('GET /production', () => {
      return request(app.getHttpServer()).get('/api/v1/production').expect(200).expect([]);
    });

    it('POST /production', () => {
      return request(app.getHttpServer())
        .post('/api/v1/production')
        .send({
          productType: 'LECHE',
          cantidad: 25,
          unidad: 'L',
          fecha: '2026-01-14',
          jornada: 'MANANA',
          animalId: TEST_ENTITY_ID,
        })
        .expect(201);
    });

    it('PATCH /production/:id', () => {
      return request(app.getHttpServer())
        .patch(`/api/v1/production/${TEST_ENTITY_ID}`)
        .send({
          productType: 'LECHE',
          cantidad: 30,
          unidad: 'L',
          fecha: '2026-01-14',
          jornada: 'TARDE',
          animalId: TEST_ENTITY_ID,
        })
        .expect(200);
    });

    it('DELETE /production/:id', () => {
      return request(app.getHttpServer()).delete(`/api/v1/production/${TEST_ENTITY_ID}`).expect(200);
    });
  });

  describe('reproduction', () => {
    it('GET /reproduction/summary', () => {
      return request(app.getHttpServer())
        .get('/api/v1/reproduction/summary')
        .expect(200)
        .expect((res) => {
          expect(res.body.enCelo).toBeDefined();
        });
    });

    it('GET /reproduction', () => {
      return request(app.getHttpServer()).get('/api/v1/reproduction').expect(200).expect([]);
    });

    it('POST /reproduction', () => {
      return request(app.getHttpServer())
        .post('/api/v1/reproduction')
        .send({
          animalId: TEST_ENTITY_ID,
          tipoEvento: 'INSEMINACION',
          programada: '2026-03-01',
        })
        .expect(201);
    });

    it('PATCH /reproduction/:id', () => {
      return request(app.getHttpServer())
        .patch(`/api/v1/reproduction/${TEST_ENTITY_ID}`)
        .send({
          animalId: TEST_ENTITY_ID,
          tipoEvento: 'PARTO',
          programada: '2026-03-01',
        })
        .expect(200);
    });

    it('DELETE /reproduction/:id', () => {
      return request(app.getHttpServer()).delete(`/api/v1/reproduction/${TEST_ENTITY_ID}`).expect(200);
    });
  });

  describe('tasks', () => {
    it('GET /tasks', () => {
      return request(app.getHttpServer()).get('/api/v1/tasks').expect(200).expect([]);
    });

    it('POST /tasks', () => {
      return request(app.getHttpServer())
        .post('/api/v1/tasks')
        .send({
          titulo: 'Fumigar potrero',
          tipo: 'POTRERO',
          vencimiento: '2026-02-15',
          referencia: 'Potrero 1',
        })
        .expect(201);
    });

    it('PATCH /tasks/:id/complete', () => {
      return request(app.getHttpServer())
        .patch(`/api/v1/tasks/${TEST_ENTITY_ID}/complete`)
        .expect(200);
    });

    it('DELETE /tasks/:id', () => {
      return request(app.getHttpServer()).delete(`/api/v1/tasks/${TEST_ENTITY_ID}`).expect(200);
    });
  });

  describe('sync', () => {
    it('GET /sync/pull', () => {
      return request(app.getHttpServer())
        .get('/api/v1/sync/pull')
        .expect(200)
        .expect((res) => {
          expect(res.body.animals).toBeDefined();
          expect(res.body.potreros).toBeDefined();
        });
    });

    it('POST /sync/push', () => {
      return request(app.getHttpServer())
        .post('/api/v1/sync/push')
        .send({ changes: [] })
        .expect(201)
        .expect((res) => {
          expect(res.body.applied).toEqual([]);
        });
    });
  });

  describe('reports', () => {
    it('GET /reports/weekly', () => {
      return request(app.getHttpServer())
        .get('/api/v1/reports/weekly')
        .query({ from: '2026-01-01', to: '2026-01-07' })
        .expect(200)
        .expect((res) => {
          expect(res.body.recordCount).toBeDefined();
        });
    });

    it('GET /reports/monthly', () => {
      return request(app.getHttpServer())
        .get('/api/v1/reports/monthly')
        .query({ year: '2026', month: '1' })
        .expect(200)
        .expect((res) => {
          expect(res.body.recordCount).toBeDefined();
        });
    });
  });
});
