import axios from "axios";

/**
 * Cliente axios centralizado para toda la aplicación.
 *
 * - `baseURL`      → URL del backend, configurable por ambiente (VITE_API_URL).
 * - `withCredentials: true` → envía y recibe la cookie de sesión (JSESSIONID),
 *   necesaria porque la autenticación del backend es por HttpSession.
 *
 * El backend responde SIEMPRE con el envoltorio `ApiResponse`:
 *   { success: boolean, message: string, data: T }
 */
const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    withCredentials: true,
    headers: {
        "Content-Type": "application/json"
    }
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
