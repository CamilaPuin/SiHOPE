import api from "./api";

export const listCareers = () =>
    api.get("/api/admin/carreras").then((r) => r.data);

export const createCareer = (nombre) =>
    api.post("/api/admin/carreras", { nombre }).then((r) => r.data);

export const deleteCareer = (id) =>
    api.delete(`/api/admin/carreras/${id}`).then((r) => r.data);
