import api from "./api";

/**
 * Servicio de autenticación → controller LoginController (/api/auth).
 * Cada función devuelve el envoltorio ApiResponse: { success, message, data }.
 */

// POST /api/auth/login  ·  body: { correo, password }
export const login = (credenciales) =>
    api.post("/api/auth/login", credenciales).then((r) => r.data);

// POST /api/auth/logout
export const logout = () =>
    api.post("/api/auth/logout").then((r) => r.data);

// GET /api/auth/me  ·  usuario de la sesión actual (401 si no hay sesión)
export const me = () =>
    api.get("/api/auth/me").then((r) => r.data);
