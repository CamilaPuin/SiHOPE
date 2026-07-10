import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { PageLoader } from "./common/Spinner";


export default function RutaProtegida() {
    const { estaAutenticado, cargando } = useAuth();
    const location = useLocation();

    if (cargando) return <PageLoader mensaje="Verificando sesión…" />;

    if (!estaAutenticado) {
        return <Navigate to="/login" replace state={{ desde: location.pathname }} />;
    }

    return <Outlet />;
}
