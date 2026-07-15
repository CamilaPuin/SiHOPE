import api from "./api";

export const register = (data) =>
    api.post("/api/registro", data).then((r) => r.data);

export const verify = (token) =>
    api.get("/api/registro/verificar", { params: { token } }).then((r) => r.data);

export const listRegistrationCareers = () =>
    api.get("/api/registro/carreras").then((r) => r.data);
