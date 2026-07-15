import api from "./api";

export const listOpen = () =>
    api.get("/api/convocatorias").then((r) => r.data);

export const detail = (id) =>
    api.get(`/api/convocatorias/${id}`).then((r) => r.data);

export const apply = (id, datos) =>
    api.post(`/api/convocatorias/${id}/postulaciones`, { datos }).then((r) => r.data);

export const myApplications = () =>
    api.get("/api/convocatorias/mis-postulaciones").then((r) => r.data);

export const createVacancy = (data) =>
    api.post("/api/coordinador/convocatorias", data).then((r) => r.data);

export const listAll = () =>
    api.get("/api/coordinador/convocatorias").then((r) => r.data);

export const closeVacancy = (id) =>
    api.patch(`/api/coordinador/convocatorias/${id}/cerrar`).then((r) => r.data);

export const listApplications = (id) =>
    api.get(`/api/coordinador/convocatorias/${id}/postulaciones`).then((r) => r.data);

export const changeApplicationStatus = (id, estado) =>
    api.patch(`/api/coordinador/postulaciones/${id}/estado`, { estado }).then((r) => r.data);

export const promoteToMonitor = (id) =>
    api.post(`/api/coordinador/postulaciones/${id}/promover`).then((r) => r.data);
