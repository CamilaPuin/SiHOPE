import api from "./api";

/**
 * Servicio de convocatorias y postulaciones (HU_005 / HU_008 / HU_009).
 * Cada función devuelve el envoltorio ApiResponse: { success, message, data }.
 */

/* ---- Aspirante (ESTUDIANTE) → ConvocatoriaController /api/convocatorias ---- */

// GET /api/convocatorias  ·  data: ConvocatoriaResponse[] (abiertas)
export const listarAbiertas = () =>
    api.get("/api/convocatorias").then((r) => r.data);

// GET /api/convocatorias/{id}
export const detalle = (id) =>
    api.get(`/api/convocatorias/${id}`).then((r) => r.data);

// POST /api/convocatorias/{id}/postulaciones  ·  body: { datos: { campo: valor } }
export const postular = (id, datos) =>
    api.post(`/api/convocatorias/${id}/postulaciones`, { datos }).then((r) => r.data);

/* ---- Coordinador → CoordinadorController /api/coordinador ---- */

// POST /api/coordinador/convocatorias  ·  body: { titulo, descripcion, requisitos, materia, plazas, fechaLimite }
export const crearConvocatoria = (datos) =>
    api.post("/api/coordinador/convocatorias", datos).then((r) => r.data);

// GET /api/coordinador/convocatorias  ·  data: ConvocatoriaResponse[] (todas)
export const listarTodas = () =>
    api.get("/api/coordinador/convocatorias").then((r) => r.data);

// PATCH /api/coordinador/convocatorias/{id}/cerrar
export const cerrarConvocatoria = (id) =>
    api.patch(`/api/coordinador/convocatorias/${id}/cerrar`).then((r) => r.data);

// GET /api/coordinador/convocatorias/{id}/postulaciones  ·  data: PostulacionResponse[]
export const listarPostulaciones = (id) =>
    api.get(`/api/coordinador/convocatorias/${id}/postulaciones`).then((r) => r.data);

// PATCH /api/coordinador/postulaciones/{id}/estado  ·  body: { estado: "APROBADA" | "RECHAZADA" }
export const cambiarEstadoPostulacion = (id, estado) =>
    api.patch(`/api/coordinador/postulaciones/${id}/estado`, { estado }).then((r) => r.data);

// POST /api/coordinador/postulaciones/{id}/promover
export const promoverAMonitor = (id) =>
    api.post(`/api/coordinador/postulaciones/${id}/promover`).then((r) => r.data);
