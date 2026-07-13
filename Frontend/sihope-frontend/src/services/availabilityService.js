import api from "./api";

export const myAvailability = () =>
    api.get("/api/monitor/disponibilidad").then((r) => r.data);

export const saveAvailability = (bloques) =>
    api.put("/api/monitor/disponibilidad", { bloques }).then((r) => r.data);

export const availabilityOf = (monitorId) =>
    api.get(`/api/monitores/${monitorId}/disponibilidad`).then((r) => r.data);

export const listMonitors = (asignaturaId) =>
    api
        .get("/api/monitores", {
            params: asignaturaId ? { asignaturaId } : undefined
        })
        .then((r) => r.data);
