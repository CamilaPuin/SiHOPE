import axios from "axios";
import { getToken, clearToken } from "../utils/token";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        "Content-Type": "application/json"
    }
});

api.interceptors.request.use((config) => {
    const token = getToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export class ApiError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.data = data;
    }
}

api.interceptors.response.use(
    (response) => response,
    (error) => {
        const response = error.response;
        if (response) {
            if (response.status === 401) {
                clearToken();
            }
            const body = response.data || {};
            const message =
                body.message ||
                "Ocurrió un error procesando la solicitud. Inténtalo de nuevo.";
            return Promise.reject(new ApiError(message, response.status, body.data));
        }
        return Promise.reject(
            new ApiError(
                "No se pudo conectar con el servidor. Verifica tu conexión e inténtalo de nuevo.",
                0,
                null
            )
        );
    }
);

export default api;
