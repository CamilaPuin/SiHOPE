import api from "./api";

export const freeSlots = (monitorId, fecha) =>
    api
        .get(`/api/monitores/${monitorId}/horarios-disponibles`, { params: { fecha } })
        .then((r) => r.data);

export const myCitas = () => api.get("/api/citas").then((r) => r.data);

export const bookCita = (payload) =>
    api.post("/api/citas", payload).then((r) => r.data);

export const confirmCita = (id) =>
    api.patch(`/api/citas/${id}/confirmar`).then((r) => r.data);

export const cancelCita = (id, motivo) =>
    api.patch(`/api/citas/${id}/cancelar`, { motivo }).then((r) => r.data);

export const attendCita = (id) =>
    api.patch(`/api/citas/${id}/atender`).then((r) => r.data);
