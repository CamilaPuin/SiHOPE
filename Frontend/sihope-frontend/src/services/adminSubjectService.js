import api from "./api";

export const listSubjects = () =>
    api.get("/api/admin/asignaturas").then((r) => r.data);

export const createSubject = (nombre) =>
    api.post("/api/admin/asignaturas", { nombre }).then((r) => r.data);

export const deleteSubject = (id) =>
    api.delete(`/api/admin/asignaturas/${id}`).then((r) => r.data);
