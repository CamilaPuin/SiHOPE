import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { panelPorRol } from "../utils/roles";

/**
 * Restringe el acceso a uno o varios roles. Si el rol del usuario no está
 * autorizado, lo redirige a su panel principal.
 *
 * @param {string|string[]} rol  rol(es) permitido(s).
 */
export default function RutaRol({ rol }) {
    const { usuario } = useAuth();
    const permitidos = Array.isArray(rol) ? rol : [rol];

    if (!usuario || !permitidos.includes(usuario.rol)) {
        return <Navigate to={panelPorRol(usuario?.rol)} replace />;
    }

    return <Outlet />;
}
