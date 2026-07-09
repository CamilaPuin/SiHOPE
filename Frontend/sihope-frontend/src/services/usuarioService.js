import api from "./api";

/**
 * Servicio de administración de usuarios → controller UserController
 * (/api/admin/usuarios). Solo accesible para el rol ADMINISTRADOR.
 * Cada función devuelve el envoltorio ApiResponse: { success, message, data }.
 */

// GET /api/admin/usuarios  ·  data: UserResponse[]
export const listar = () =>
    api.get("/api/admin/usuarios").then((r) => r.data);

// POST /api/admin/usuarios  ·  body: { nombre, correo, documento, rol }
// En caso de errores de validación, `data` es un mapa { campo: mensaje }.
export const crear = (datos) =>
    api.post("/api/admin/usuarios", datos).then((r) => r.data);

// PUT /api/admin/usuarios/{id}/rol  ·  body: { rol }
export const cambiarRol = (id, rol) =>
    api.put(`/api/admin/usuarios/${id}/rol`, { rol }).then((r) => r.data);

// PATCH /api/admin/usuarios/{id}/estado  ·  data: boolean (nuevo estado activo)
export const cambiarEstado = (id) =>
    api.patch(`/api/admin/usuarios/${id}/estado`).then((r) => r.data);
