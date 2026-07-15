import api from "./api";

export const listAsignaturas = () =>
    api.get("/api/asignaturas").then((r) => r.data);

// Solo lectura: las asignaturas del monitor las asigna el coordinador.
export const mySubjects = () =>
    api.get("/api/monitor/asignaturas").then((r) => r.data);

export const monitorSubjects = (monitorId) =>
    api.get(`/api/coordinador/monitores/${monitorId}/asignaturas`).then((r) => r.data);

export const assignMonitorSubjects = (monitorId, asignaturaIds) =>
    api
        .put(`/api/coordinador/monitores/${monitorId}/asignaturas`, { asignaturaIds })
        .then((r) => r.data);
