import api from "./api";
import { guardarToken, borrarToken } from "../utils/token";

/**
 * Servicio de autenticación → controller LoginController (/api/auth).
 * Cada función devuelve el envoltorio ApiResponse: { success, message, data }.
 *
 * Tras la migración a JWT, `login` guarda el token (data = { token, usuario }) y
 * `logout` lo elimina; el token viaja como Bearer en cada petición (services/api.js).
 */

// POST /api/auth/login  ·  body: { correo, password }  ·  data: { token, usuario }
export const login = (credenciales) =>
    api.post("/api/auth/login", credenciales).then((r) => {
        const cuerpo = r.data;
        if (cuerpo?.data?.token) {
            guardarToken(cuerpo.data.token);
        }
        return cuerpo;
    });

// POST /api/auth/logout  ·  stateless: basta con descartar el token local.
export const logout = () =>
    api.post("/api/auth/logout").finally(() => borrarToken()).then((r) => r.data);

// GET /api/auth/me  ·  usuario asociado al token (401 si no es válido)
export const me = () =>
    api.get("/api/auth/me").then((r) => r.data);
