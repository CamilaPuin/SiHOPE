import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { panelPorRol } from "../utils/roles";

export default function RutaRol({ rol }) {
    const { usuario } = useAuth();
    const permitidos = Array.isArray(rol) ? rol : [rol];

    if (!usuario || !permitidos.includes(usuario.rol)) {
        return <Navigate to={panelPorRol(usuario?.rol)} replace />;
    }

    return <Outlet />;
}
