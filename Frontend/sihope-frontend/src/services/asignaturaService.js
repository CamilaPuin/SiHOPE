import api from "./api";

export const listAsignaturas = () =>
    api.get("/api/asignaturas").then((r) => r.data);

export const mySubjects = () =>
    api.get("/api/monitor/asignaturas").then((r) => r.data);

export const saveMySubjects = (asignaturas) =>
    api.put("/api/monitor/asignaturas", { asignaturas }).then((r) => r.data);
