/** Roles válidos del sistema (deben coincidir con UserService.ROLES_VALIDOS del backend). */
export const ROLES = ["ESTUDIANTE", "MONITOR", "COORDINADOR", "ADMINISTRADOR"];

/**
 * Ruta del panel principal según el rol, replicando la lógica del layout Thymeleaf:
 *   ADMINISTRADOR → /admin/usuarios · MONITOR → /monitor
 *   COORDINADOR   → /coordinador    · resto   → /home
 */
export function panelPorRol(rol) {
    switch (rol) {
        case "ADMINISTRADOR":
            return "/admin/usuarios";
        case "MONITOR":
            return "/monitor";
        case "COORDINADOR":
            return "/coordinador";
        default:
            return "/home";
    }
}
