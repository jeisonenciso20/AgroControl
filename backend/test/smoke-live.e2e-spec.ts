import request from 'supertest';

const LIVE = process.env.RUN_LIVE_E2E === '1';
const BASE = process.env.API_BASE ?? 'http://127.0.0.1:3000';

const demo = {
  email: 'demo@agrocontrol.local',
  password: 'demo1234',
};

(LIVE ? describe : describe.skip)('Smoke live API (Postgres + running server)', () => {
  let accessToken: string;

  it('POST /auth/login', async () => {
    const res = await request(BASE)
      .post('/api/v1/auth/login')
      .send(demo)
      .expect(201);
    accessToken = res.body.accessToken;
    expect(accessToken).toBeTruthy();
  });

  function authGet(path: string) {
    return request(BASE).get(path).set('Authorization', `Bearer ${accessToken}`);
  }

  function authPost(path: string, body: object) {
    return request(BASE).post(path).set('Authorization', `Bearer ${accessToken}`).send(body);
  }

  it('GET /dashboard/summary', () => {
    return authGet('/api/v1/dashboard/summary').expect(200);
  });

  it('GET /sync/pull', () => {
    return authGet('/api/v1/sync/pull').expect(200);
  });

  it('POST /sync/push (empty changes)', () => {
    return authPost('/api/v1/sync/push', { changes: [] }).expect(201);
  });

  it('GET /reports/weekly', () => {
    return authGet('/api/v1/reports/weekly')
      .query({ from: '2026-01-01', to: '2026-01-07' })
      .expect(200);
  });

  it('GET /reports/monthly', () => {
    return authGet('/api/v1/reports/monthly').query({ year: '2026', month: '1' }).expect(200);
  });

  it('GET /tasks', () => {
    return authGet('/api/v1/tasks').expect(200);
  });

  it('GET /animals', () => {
    return authGet('/api/v1/animals').expect(200);
  });

  it('POST /animals + DELETE /animals/:id', async () => {
    const codigo = `E2E-${Date.now()}`;
    const created = await authPost('/api/v1/animals', {
      codigo,
      tipo: 'Vaca',
      raza: 'Test',
      sexo: 'Hembra',
      peso: 100,
      fechaNacimiento: '2020-01-01',
    }).expect(201);
    await request(BASE)
      .delete(`/api/v1/animals/${created.body.id}`)
      .set('Authorization', `Bearer ${accessToken}`)
      .expect(200);
  });
});
