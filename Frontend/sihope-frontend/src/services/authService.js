import api from "./api";
import { saveToken, clearToken } from "../utils/token";

export const login = (credentials) =>
    api.post("/api/auth/login", credentials).then((r) => {
        const body = r.data;
        if (body?.data?.token) {
            saveToken(body.data.token);
        }
        return body;
    });

export const logout = () =>
    api.post("/api/auth/logout").finally(() => clearToken()).then((r) => r.data);

export const me = () =>
    api.get("/api/auth/me").then((r) => r.data);
