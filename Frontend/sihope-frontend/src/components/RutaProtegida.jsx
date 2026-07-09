import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { PageLoader } from "./common/Spinner";

/**
 * Envuelve rutas que requieren sesión. Mientras se resuelve la sesión inicial
 * muestra un cargador; si no hay usuario, redirige a /login recordando el
 * destino para volver tras autenticarse.
 */
export default function RutaProtegida() {
    const { estaAutenticado, cargando } = useAuth();
    const location = useLocation();

    if (cargando) return <PageLoader mensaje="Verificando sesión…" />;

    if (!estaAutenticado) {
        return <Navigate to="/login" replace state={{ desde: location.pathname }} />;
    }

    return <Outlet />;
}
