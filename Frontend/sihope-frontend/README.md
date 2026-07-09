# SiHope · Frontend (React + Vite)

Interfaz de la plataforma de monitorías académicas de la UPTC. Consume la API
REST del backend Spring Boot (carpeta `../../SiHope`) mediante un cliente axios
centralizado y autenticación por sesión (cookie `JSESSIONID`).

## Requisitos

- Node.js 18+ y npm
- El backend corriendo (ver `../../SiHope/README.md`)

## Variables de entorno

Toda variable expuesta al navegador debe empezar por `VITE_`.

| Variable       | Descripción                          | Ejemplo (dev)           |
| -------------- | ------------------------------------ | ----------------------- |
| `VITE_API_URL` | URL base del backend REST (sin `/`)  | `http://localhost:8080` |

Archivos:

- `.env` → desarrollo local (`VITE_API_URL=http://localhost:8080`).
- `.env.production` → valor por defecto para `npm run build` (se recomienda
  sobreescribir en Vercel).
- `.env.example` → plantilla de referencia.

## Desarrollo local

```bash
npm install
npm run dev        # http://localhost:5173
```

> El origen `http://localhost:5173` ya está permitido por CORS en el backend.
> Si cambias el puerto, ajusta `APP_CORS_ALLOWED_ORIGINS` en el backend.

Otros scripts:

```bash
npm run build      # genera dist/
npm run preview    # sirve el build de producción
npm run lint       # eslint
```

## Estructura

```
src/
├── services/        # cliente axios (api.js) + un servicio por controller
│   ├── api.js               # instancia axios (baseURL, withCredentials, interceptor de errores)
│   ├── authService.js       # /api/auth
│   ├── registroService.js   # /api/registro
│   ├── credencialesService.js # /api/credenciales
│   └── usuarioService.js    # /api/admin/usuarios
├── context/         # AuthProvider + AuthContext (estado de sesión)
├── hooks/           # useAuth
├── components/
│   ├── layout/      # AppShell, Sidebar, Topbar, Footer, Isotipo
│   ├── common/      # Alert, Spinner, Field, PasswordRequirements
│   ├── RutaProtegida.jsx   # guarda de sesión
│   └── RutaRol.jsx         # guarda por rol
├── pages/           # una carpeta por pantalla (Login, Registro, ...)
├── routes/AppRoutes.jsx    # definición de rutas
├── utils/           # roles.js, password.js
└── styles/          # sihope.css (marca) + app.css (extras React)
```

## Autenticación

La sesión es por **cookie** (`JSESSIONID`), no por token. El cliente axios usa
`withCredentials: true` para enviarla en cada petición. Al arrancar, la app llama
a `GET /api/auth/me` para restaurar la sesión; `RutaProtegida` bloquea las rutas
privadas mientras no haya usuario.

## Manejo de estados

Cada acción del usuario dispara una petición HTTP y refleja:

- **carga** → botón deshabilitado + `Spinner`
- **error** → componente `Alert` con el mensaje del backend (o mapa de errores
  por campo en registro / creación de usuario)
- **éxito** → `Alert`/redirección; en el panel de admin, `SweetAlert2` para
  confirmaciones y toasts.

## Despliegue en Vercel

1. Importa el proyecto y selecciona **Root Directory** = `Frontend/sihope-frontend`.
2. Framework: **Vite** (detectado automáticamente). Build: `npm run build`,
   Output: `dist` (definidos también en `vercel.json`).
3. En **Settings → Environment Variables** define `VITE_API_URL` con la URL
   pública del backend (p. ej. `https://api.tu-dominio.com`).
4. `vercel.json` ya incluye el *rewrite* SPA para que React Router funcione en
   rutas profundas (p. ej. recargar `/admin/usuarios`).
5. En el backend, añade el dominio de Vercel a `APP_CORS_ALLOWED_ORIGINS`.
