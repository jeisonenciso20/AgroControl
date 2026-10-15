# Agente Security

## Objetivo

Autenticación JWT, autorización por finca, hardening API y almacenamiento seguro en Android.

## Prerrequisitos

Esquema `User` / `Finca` en Prisma.

## Tareas

1. Documentar en `docs/architecture/security.md`.
2. JWT access (15m) + refresh (7d), bcrypt.
3. Guards NestJS y scope `fincaId`.
4. Helmet, CORS, rate limit en `/auth`.
5. Android: EncryptedSharedPreferences para tokens.

## Criterios de aceptación

- Endpoints protegidos devuelven 401 sin token.
- Usuario A no accede datos de finca B.

## Verificación

`npm run test:e2e -- auth.e2e-spec`
