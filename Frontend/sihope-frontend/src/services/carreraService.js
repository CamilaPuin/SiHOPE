import api from "./api";

export const listCareers = () =>
    api.get("/api/carreras").then((r) => r.data);
