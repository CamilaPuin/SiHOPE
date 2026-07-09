import { Routes, Route, Navigate } from "react-router-dom";

import RutaProtegida from "../components/RutaProtegida";
import RutaRol from "../components/RutaRol";
import AppShell from "../components/layout/AppShell";
import { useAuth } from "../hooks/useAuth";
import { panelPorRol } from "../utils/roles";
import { PageLoader } from "../components/common/Spinner";

// Páginas públicas
import Login from "../pages/Login/Login";
import Registro from "../pages/Registro/Registro";
import Recuperar from "../pages/Recuperar/Recuperar";
import Restablecer from "../pages/Restablecer/Restablecer";
import Verificar from "../pages/Verificar/Verificar";

// Páginas internas
import Home from "../pages/Home/Home";
import Coordinador from "../pages/Coordinador/Coordinador";
import Monitor from "../pages/Monitor/Monitor";
import Monitores from "../pages/Monitores/Monitores";
import Credenciales from "../pages/Credenciales/Credenciales";
import AdminUsuarios from "../pages/AdminUsuarios/AdminUsuarios";

/** Redirige la raíz "/" al panel del rol (o a /login si no hay sesión). */
function Raiz() {
    const { usuario, cargando } = useAuth();
    if (cargando) return <PageLoader mensaje="Cargando…" />;
    if (!usuario) return <Navigate to="/login" replace />;
    return <Navigate to={panelPorRol(usuario.rol)} replace />;
}

export default function AppRoutes() {
    return (
        <Routes>
            {/* Públicas */}
            <Route path="/login" element={<Login />} />
            <Route path="/registro" element={<Registro />} />
            <Route path="/recuperar" element={<Recuperar />} />
            <Route path="/restablecer" element={<Restablecer />} />
            <Route path="/verificar" element={<Verificar />} />

            {/* Protegidas (requieren sesión) dentro del layout con barra lateral */}
            <Route element={<RutaProtegida />}>
                <Route element={<AppShell />}>
                    <Route path="/home" element={<Home />} />
                    <Route path="/coordinador" element={<Coordinador />} />
                    <Route path="/monitor" element={<Monitor />} />
                    <Route path="/monitores" element={<Monitores />} />
                    <Route path="/credenciales" element={<Credenciales />} />

                    {/* Solo administrador */}
                    <Route element={<RutaRol rol="ADMINISTRADOR" />}>
                        <Route path="/admin/usuarios" element={<AdminUsuarios />} />
                    </Route>
                </Route>
            </Route>

            {/* Raíz y comodín */}
            <Route path="/" element={<Raiz />} />
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}
