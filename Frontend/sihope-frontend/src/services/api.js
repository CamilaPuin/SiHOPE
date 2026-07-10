import axios from "axios";
import { obtenerToken, borrarToken } from "../utils/token";

/**
 * Cliente axios centralizado para toda la aplicación.
 *
 * - `baseURL` → URL del backend, configurable por ambiente (VITE_API_URL).
 * - Autenticación por JWT: cada petición adjunta `Authorization: Bearer <token>`
 *   leído de localStorage (ver utils/token.js). Si el backend responde 401, el
 *   token se descarta para forzar un nuevo inicio de sesión.
 *
 * El backend responde SIEMPRE con el envoltorio `ApiResponse`:
 *   { success: boolean, message: string, data: T }
 */
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    headers: {
        "Content-Type": "application/json"
    }
});

// Interceptor de petición: adjunta el token JWT si existe.
api.interceptors.request.use((config) => {
    const token = obtenerToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

/**
 * Error normalizado que exponen todos los servicios.
 * Cualquier `catch` puede leer `err.message` (texto para el usuario),
 * `err.status` (código HTTP) y `err.data` (p. ej. el mapa de errores por campo).
 */
export class ApiError extends Error {
    constructor(message, status, data) {
        super(message);
        this.name = "ApiError";
        this.status = status;
        this.data = data;
    }
}

// Interceptor de respuesta: normaliza cualquier error a un ApiError uniforme.
api.interceptors.response.use(
    (response) => response,
    (error) => {
        const respuesta = error.response;
        if (respuesta) {
            // Token inválido/expirado o rol revocado → descartar el token local.
            if (respuesta.status === 401) {
                borrarToken();
            }
            // El backend devolvió un ApiResponse de error → reutilizamos su mensaje/data.
            const cuerpo = respuesta.data || {};
            const mensaje =
                cuerpo.message ||
                "Ocurrió un error procesando la solicitud. Inténtalo de nuevo.";
            return Promise.reject(new ApiError(mensaje, respuesta.status, cuerpo.data));
        }
        // Sin respuesta: problema de red, CORS o backend caído.
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
