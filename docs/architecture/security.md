# Seguridad

## Autenticación

- Registro crea `Finca` + `User`.
- Login devuelve `accessToken` (15m) y `refreshToken` (7d).
- Refresh rota el refresh token.

## Autorización

- `JwtAuthGuard` en rutas protegidas.
- `fincaId` del JWT filtra todas las consultas Prisma.

## API

- Helmet headers.
- Throttler en rutas `/auth/*` (10 req/min por IP).
- Validación DTO con `class-validator`.
- Secretos solo en `.env` (no commitear).

## Android

- Tokens en `EncryptedSharedPreferences` (`TokenStore`).
- `INTERNET` + cleartext solo debug (`usesCleartextTraffic`).

## Checklist revisión

- [ ] Sin passwords en logs
- [ ] 401 en recursos sin token
- [ ] 403/404 al acceder recurso de otra finca
- [ ] bcrypt cost >= 10
