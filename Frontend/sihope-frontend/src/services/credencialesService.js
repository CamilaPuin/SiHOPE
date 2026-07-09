import api from "./api";

/**
 * Servicio de credenciales → controller CredencialesController (/api/credenciales).
 * Cada función devuelve el envoltorio ApiResponse: { success, message, data }.
 */

// PUT /api/credenciales/password  ·  body: { actual, nueva, nueva2 }  (requiere sesión)
export const cambiarPassword = (datos) =>
    api.put("/api/credenciales/password", datos).then((r) => r.data);

// POST /api/credenciales/recuperar  ·  body: { correo }
export const recuperar = (correo) =>
    api.post("/api/credenciales/recuperar", { correo }).then((r) => r.data);

// POST /api/credenciales/restablecer  ·  body: { token, nueva, nueva2 }
export const restablecer = (datos) =>
    api.post("/api/credenciales/restablecer", datos).then((r) => r.data);
