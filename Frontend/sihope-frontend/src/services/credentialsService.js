import api from "./api";

export const changePassword = (data) =>
    api.put("/api/credenciales/password", data).then((r) => r.data);

export const recover = (correo) =>
    api.post("/api/credenciales/recuperar", { correo }).then((r) => r.data);

export const reset = (data) =>
    api.post("/api/credenciales/restablecer", data).then((r) => r.data);
