# SiHope · Backend (Spring Boot REST API)

API REST de la plataforma de monitorías académicas de la UPTC. Expone endpoints
JSON bajo `/api/**`, consumidos por el frontend React (`../Frontend/sihope-frontend`).

- **Java 17**, **Spring Boot 4.1**, **MySQL 8**, JPA/Hibernate.
- Autenticación por **HttpSession** (cookie `JSESSIONID`). Hash de contraseñas con BCrypt.
- Todas las respuestas usan el envoltorio `ApiResponse`:
  ```json
  { "success": true, "message": "texto", "data": { } }
  ```

## Ejecución con Docker (recomendado)

Levanta backend + base de datos con un solo comando:

```bash
docker-compose up --build
```

- API en `http://localhost:8080`
- MySQL en `localhost:13306` (dentro de la red de compose: `mysql:3306`)

El servicio `app` espera a que MySQL esté saludable (`healthcheck`) antes de
arrancar. Para detener: `docker-compose down` (agrega `-v` para borrar los datos).

## Ejecución local (sin Docker para la app)

Necesitas solo la base de datos en Docker:

```bash
docker-compose up -d mysql
./mvnw spring-boot:run          # usa DB_URL por defecto jdbc:mysql://localhost:13306/library
```

## Variables de entorno

Definidas con valores por defecto en `application.properties` y `docker-compose.yaml`
(ver también `.env.example`):

| Variable                    | Por defecto                                   | Descripción                                   |
| --------------------------- | --------------------------------------------- | --------------------------------------------- |
| `DB_URL`                    | `jdbc:mysql://localhost:13306/library`        | URL JDBC de MySQL (en compose: `mysql:3306`)  |
| `DB_USERNAME`               | `user`                                        | Usuario de la base de datos                   |
| `DB_PASSWORD`               | `library2026`                                 | Contraseña de la base de datos                |
| `APP_CORS_ALLOWED_ORIGINS`  | `http://localhost:5173`                       | Orígenes permitidos por CORS (coma-separados) |

> **Producción / Vercel:** añade el dominio del frontend a
> `APP_CORS_ALLOWED_ORIGINS`, p. ej. `https://sihope.vercel.app`. CORS ya permite
> credenciales (`allowCredentials(true)`), necesario para la cookie de sesión.

## Usuarios sembrados (DataInitializer)

Se crean automáticamente al arrancar (activos y verificados):

| Rol            | Correo                     | Contraseña       |
| -------------- | -------------------------- | ---------------- |
| ADMINISTRADOR  | `admin@uptc.edu.co`        | `Admin2026*`     |
| COORDINADOR    | `coordinador@uptc.edu.co`  | `Coord2026*`     |
| MONITOR        | `monitor@uptc.edu.co`      | `Monitor2026*`   |
| ESTUDIANTE     | `estudiante@uptc.edu.co`   | `Estudiante2026*`|

## Endpoints

Base URL: `http://localhost:8080`. Cuerpo y respuesta en JSON.

### Autenticación — `LoginController` (`/api/auth`)

| Método | Ruta               | Cuerpo                    | Acción                              |
| ------ | ------------------ | ------------------------- | ----------------------------------- |
| POST   | `/api/auth/login`  | `{ correo, password }`    | Inicia sesión; crea la sesión.      |
| GET    | `/api/auth/me`     | —                         | Usuario de la sesión (401 sin sesión). |
| POST   | `/api/auth/logout` | —                         | Cierra la sesión.                   |

### Registro — `RegistroController` (`/api/registro`)

| Método | Ruta                        | Cuerpo / Query                                         | Acción                        |
| ------ | --------------------------- | ------------------------------------------------------ | ----------------------------- |
| POST   | `/api/registro`             | `{ nombres, apellidos, codigo, correo, password, password2 }` | Registro de estudiante.       |
| GET    | `/api/registro/verificar`   | `?token=...`                                           | Verifica la cuenta.           |

### Credenciales — `CredencialesController` (`/api/credenciales`)

| Método | Ruta                            | Cuerpo                     | Acción                             |
| ------ | ------------------------------- | -------------------------- | ---------------------------------- |
| PUT    | `/api/credenciales/password`    | `{ actual, nueva, nueva2 }`| Cambia la contraseña (requiere sesión). |
| POST   | `/api/credenciales/recuperar`   | `{ correo }`               | Solicita enlace de recuperación.   |
| POST   | `/api/credenciales/restablecer` | `{ token, nueva, nueva2 }` | Restablece con token.              |

### Administración de usuarios — `UserController` (`/api/admin/usuarios`, solo ADMINISTRADOR)

| Método | Ruta                               | Cuerpo                                | Acción                          |
| ------ | ---------------------------------- | ------------------------------------- | ------------------------------- |
| GET    | `/api/admin/usuarios`              | —                                     | Lista todos los usuarios.       |
| POST   | `/api/admin/usuarios`              | `{ nombre, correo, documento, rol }`  | Crea un usuario.                |
| PUT    | `/api/admin/usuarios/{id}/rol`     | `{ rol }`                             | Cambia el rol.                  |
| PATCH  | `/api/admin/usuarios/{id}/estado`  | —                                     | Activa/desactiva (devuelve el nuevo estado). |

**Códigos HTTP:** 200/201 éxito · 400 validación (en registro/creación, `data`
es un mapa `{ campo: mensaje }`) · 401 sin sesión · 403 rol insuficiente ·
404 no encontrado · 500 error interno. Los errores se centralizan en
`GlobalExceptionHandler`.

## Notas

- **Correo simulado:** `EmailService` no envía correos reales; **imprime en la
  consola** los enlaces de verificación y recuperación. Copia el enlace del log
  y ábrelo en el frontend (`/verificar?token=...`, `/restablecer?token=...`).
- La guarda de sesión y el chequeo de rol admin viven en `SesionInterceptor`
  (registrado en `WebConfig` para `/api/admin/**` y `/api/credenciales/password`).
