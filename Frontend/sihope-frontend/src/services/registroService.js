import api from "./api";

/**
 * Servicio de registro y verificación → controller RegistroController (/api/registro).
 * Cada función devuelve el envoltorio ApiResponse: { success, message, data }.
 */

// POST /api/registro  ·  body: { nombres, apellidos, codigo, correo, password, password2 }
// En caso de errores de validación, `data` es un mapa { campo: mensaje }.
export const registrar = (datos) =>
    api.post("/api/registro", datos).then((r) => r.data);

// GET /api/registro/verificar?token=...
export const verificar = (token) =>
    api.get("/api/registro/verificar", { params: { token } }).then((r) => r.data);
