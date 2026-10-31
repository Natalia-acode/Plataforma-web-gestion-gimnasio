# GymFlex Frontend

React + Vite + TypeScript. Consume la API Spring Boot de `Backend/` (contrato en `docs/api/openapi.json`).

## Arranque
1. Levanta el backend (puerto 8080).
2. `cp .env.example .env` (opcional, ajusta URL y moneda).
3. `npm install` y `npm run dev` → http://localhost:5173
   El puerto debe ser 5173: es el único origen que permite el CORS del backend.

No hay usuario inicial sembrado. El formulario público de registro (`/registro`) permite elegir el tipo de cuenta:

- **Cliente**: la cuenta se activa al instante y la sesión empieza en ese momento.
- **Recepcionista, entrenador o administrador**: la cuenta nace *pendiente* y aparece en la bandeja *Solicitudes de personal* del panel de Usuarios; un administrador la aprueba (ya puede iniciar sesión) o la rechaza. Mientras el sistema no tenga ningún administrador *activo* —arranque— la primera cuenta de personal se autoactiva, porque no hay quien la revise.

El administrador también puede crear personal directamente desde ese panel (`POST /api/auth/staff`); esas cuentas nacen activas.

## Estructura
- `src/api.ts`: cliente HTTP, manejo de errores y tipos del contrato.
- `src/auth.tsx`: sesión (JWT) y normalización del rol.
- `src/App.tsx`: rutas públicas y panel por rol (cliente, recepcionista, entrenador, administrador).
- `src/pages/`: Public, Cliente, Staff, Admin.

## Pendientes del backend (ver comentarios en el código)
- `POST /api/checkin/qr` está limitado a recepcionista y administrador en `SecurityConfig`; un cliente recibe 403.
- `GET /api/usuarios` es solo de administrador; el recepcionista no puede buscar clientes (el ingreso manual pide el número de usuario).
- `GET /api/clases/{id}/asistentes` no devuelve el nombre del socio.
- Faltan endpoints de requisitos: editar perfil (RF-04), zonas (RF-33), venta presencial (RF-35), ocupación por zona (RF-16).
