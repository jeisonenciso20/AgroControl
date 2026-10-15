import request from 'supertest';

const LIVE = process.env.RUN_LIVE_E2E === '1';
const BASE = process.env.API_BASE ?? 'http://127.0.0.1:3000';

(LIVE ? describe : describe.skip)('CRUD live API (PostgreSQL)', () => {
  let token: string;
  let animalId: string;
  let potreroId: string;
  let productionId: string;
  let reproductionId: string;
  let taskId: string;

  const auth = () => ({
    Authorization: `Bearer ${token}`,
  });

  beforeAll(async () => {
    const res = await request(BASE)
      .post('/api/v1/auth/login')
      .send({ email: 'demo@agrocontrol.local', password: 'demo1234' })
      .expect(201);
    token = res.body.accessToken;

    const paddocksProbe = await request(BASE).get('/api/v1/paddocks').set({
      Authorization: `Bearer ${token}`,
    });
    if (paddocksProbe.status !== 200) {
      const detail =
        typeof paddocksProbe.body?.message === 'string'
          ? paddocksProbe.body.message
          : JSON.stringify(paddocksProbe.body);
      throw new Error(
        `Esquema desactualizado o API en error (GET /paddocks → ${paddocksProbe.status}). ` +
          `Ejecuta en backend: npm run db:setup (migrate deploy + seed) y reinicia npm run start:dev. ` +
          `Detalle: ${detail}`,
      );
    }
  });

  describe('animals', () => {
    const codigo = `E2E-${Date.now()}`;

    it('POST create', async () => {
      const res = await request(BASE)
        .post('/api/v1/animals')
        .set(auth())
        .send({
          codigo,
          nombre: 'E2E Vaca',
          tipo: 'Vaca',
          raza: 'Holstein',
          sexo: 'Hembra',
          peso: 420,
          fechaNacimiento: '2020-01-01',
          potreroNombre: 'Potrero 1',
        })
        .expect(201);
      animalId = res.body.id;
      expect(animalId).toBeTruthy();
    });

    it('GET list contains animal', async () => {
      const res = await request(BASE).get('/api/v1/animals').set(auth()).expect(200);
      expect(res.body.some((a: { codigo: string }) => a.codigo === codigo)).toBe(true);
    });

    it('GET by id', () => {
      return request(BASE).get(`/api/v1/animals/${animalId}`).set(auth()).expect(200);
    });

    it('PATCH update', async () => {
      await request(BASE)
        .patch(`/api/v1/animals/${animalId}`)
        .set(auth())
        .send({ nombre: 'E2E Actualizada' })
        .expect(200);
      const res = await request(BASE).get(`/api/v1/animals/${animalId}`).set(auth());
      expect(res.body.nombre).toBe('E2E Actualizada');
    });

    it('DELETE', async () => {
      await request(BASE).delete(`/api/v1/animals/${animalId}`).set(auth()).expect(200);
      await request(BASE).get(`/api/v1/animals/${animalId}`).set(auth()).expect(404);
    });
  });

  describe('paddocks', () => {
    const nombre = `E2E-Potrero-${Date.now()}`;

    it('POST create', async () => {
      const res = await request(BASE)
        .post('/api/v1/paddocks')
        .set(auth())
        .send({
          nombre,
          areaHectareas: 2.5,
          estado: 'DISPONIBLE',
          tipoPasto: 'Kikuyo',
          observacion: 'E2E',
        })
        .expect(201);
      potreroId = res.body.id;
    });

    it('GET by id', () => {
      return request(BASE).get(`/api/v1/paddocks/${potreroId}`).set(auth()).expect(200);
    });

    it('PATCH update', async () => {
      await request(BASE)
        .patch(`/api/v1/paddocks/${potreroId}`)
        .set(auth())
        .send({ nombre, areaHectareas: 3, estado: 'RECUPERACION' })
        .expect(200);
    });

    it('DELETE', async () => {
      await request(BASE).delete(`/api/v1/paddocks/${potreroId}`).set(auth()).expect(200);
      await request(BASE).get(`/api/v1/paddocks/${potreroId}`).set(auth()).expect(404);
    });
  });

  describe('production', () => {
    let helperAnimalId: string;

    beforeAll(async () => {
      const res = await request(BASE)
        .post('/api/v1/animals')
        .set(auth())
        .send({
          codigo: `E2E-PROD-${Date.now()}`,
          tipo: 'Vaca',
          raza: 'Jersey',
          sexo: 'Hembra',
          peso: 400,
          fechaNacimiento: '2021-01-01',
        })
        .expect(201);
      helperAnimalId = res.body.id;
    });

    it('POST create', async () => {
      const res = await request(BASE)
        .post('/api/v1/production')
        .set(auth())
        .send({
          productType: 'LECHE',
          cantidad: 15,
          unidad: 'L',
          fecha: '2026-01-10',
          jornada: 'MANANA',
          animalId: helperAnimalId,
        })
        .expect(201);
      productionId = res.body.id;
    });

    it('PATCH update', () => {
      return request(BASE)
        .patch(`/api/v1/production/${productionId}`)
        .set(auth())
        .send({
          productType: 'LECHE',
          cantidad: 16,
          unidad: 'L',
          fecha: '2026-01-10',
          jornada: 'MANANA',
          animalId: helperAnimalId,
        })
        .expect(200);
    });

    it('DELETE', async () => {
      await request(BASE).delete(`/api/v1/production/${productionId}`).set(auth()).expect(200);
      await request(BASE).delete(`/api/v1/animals/${helperAnimalId}`).set(auth()).expect(200);
    });
  });

  describe('reproduction', () => {
    let helperAnimalId: string;

    beforeAll(async () => {
      const res = await request(BASE)
        .post('/api/v1/animals')
        .set(auth())
        .send({
          codigo: `E2E-REPRO-${Date.now()}`,
          tipo: 'Vaca',
          raza: 'Holstein',
          sexo: 'Hembra',
          peso: 450,
          fechaNacimiento: '2019-01-01',
        })
        .expect(201);
      helperAnimalId = res.body.id;
    });

    it('GET summary', () => {
      return request(BASE).get('/api/v1/reproduction/summary').set(auth()).expect(200);
    });

    it('POST create', async () => {
      const res = await request(BASE)
        .post('/api/v1/reproduction')
        .set(auth())
        .send({
          animalId: helperAnimalId,
          tipoEvento: 'INSEMINACION',
          programada: '2026-02-01',
        })
        .expect(201);
      reproductionId = res.body.id;
    });

    it('PATCH update', () => {
      return request(BASE)
        .patch(`/api/v1/reproduction/${reproductionId}`)
        .set(auth())
        .send({
          animalId: helperAnimalId,
          tipoEvento: 'DIAGNOSTICO',
          programada: '2026-03-01',
        })
        .expect(200);
    });

    it('DELETE', async () => {
      await request(BASE).delete(`/api/v1/reproduction/${reproductionId}`).set(auth()).expect(200);
      await request(BASE).delete(`/api/v1/animals/${helperAnimalId}`).set(auth()).expect(200);
    });
  });

  describe('tasks', () => {
    it('POST create', async () => {
      const res = await request(BASE)
        .post('/api/v1/tasks')
        .set(auth())
        .send({
          tipo: 'POTRERO',
          vencimiento: '2026-12-01',
          referencia: 'Potrero 1',
          workType: 'FUMIGAR',
          observacion: 'E2E test',
        })
        .expect(201);
      taskId = res.body.id;
      expect(res.body.titulo).toContain('FUMIGAR');
    });

    it('PATCH complete', () => {
      return request(BASE).patch(`/api/v1/tasks/${taskId}/complete`).set(auth()).expect(200);
    });

    it('DELETE', () => {
      return request(BASE).delete(`/api/v1/tasks/${taskId}`).set(auth()).expect(200);
    });
  });
});
