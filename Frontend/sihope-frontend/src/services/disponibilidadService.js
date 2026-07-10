import api from "./api";

/**
 * Servicio de disponibilidad del monitor → MonitorController (HU_006).
 * Los bloques son { diaSemana (1-7), horaInicio "HH:mm", horaFin "HH:mm" }.
 * Cada función devuelve el envoltorio ApiResponse: { success, message, data }.
 */

// GET /api/monitor/disponibilidad  ·  data: BloqueHorario[]
export const miDisponibilidad = () =>
    api.get("/api/monitor/disponibilidad").then((r) => r.data);

// PUT /api/monitor/disponibilidad  ·  body: { bloques: BloqueHorario[] }
export const guardarDisponibilidad = (bloques) =>
    api.put("/api/monitor/disponibilidad", { bloques }).then((r) => r.data);

// GET /api/monitores/{id}/disponibilidad  ·  data: BloqueHorario[]
export const disponibilidadDe = (monitorId) =>
    api.get(`/api/monitores/${monitorId}/disponibilidad`).then((r) => r.data);
