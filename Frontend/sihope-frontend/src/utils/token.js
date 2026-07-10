/**
 * Almacenamiento del token JWT en localStorage. Es la fuente de autenticación
 * del frontend tras la migración de HttpSession a JWT: el token se adjunta a cada
 * petición como `Authorization: Bearer` (ver services/api.js).
 */
const CLAVE = "sihope_token";

export function obtenerToken() {
    return localStorage.getItem(CLAVE);
}

export function guardarToken(token) {
    if (token) localStorage.setItem(CLAVE, token);
}

export function borrarToken() {
    localStorage.removeItem(CLAVE);
}
