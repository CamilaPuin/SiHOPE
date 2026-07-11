import api from "./api";

export const list = () =>
    api.get("/api/admin/usuarios").then((r) => r.data);

export const create = (data) =>
    api.post("/api/admin/usuarios", data).then((r) => r.data);

export const changeRole = (id, rol) =>
    api.put(`/api/admin/usuarios/${id}/rol`, { rol }).then((r) => r.data);

export const changeStatus = (id) =>
    api.patch(`/api/admin/usuarios/${id}/estado`).then((r) => r.data);
