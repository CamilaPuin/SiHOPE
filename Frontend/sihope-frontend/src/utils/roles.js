export const ROLES = ["ESTUDIANTE", "MONITOR", "COORDINADOR", "ADMINISTRADOR"];

export function panelByRole(role) {
    switch (role) {
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
