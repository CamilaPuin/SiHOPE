import { Outlet, useLocation } from "react-router-dom";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";
import Footer from "./Footer";

/** Título de la barra superior según la ruta (equivale al parámetro `section`). */
const SECCIONES = {
    "/home": "Panel principal",
    "/coordinador": "Panel del coordinador",
    "/monitor": "Panel del monitor",
    "/monitores": "Monitores",
    "/credenciales": "Mi cuenta",
    "/admin/usuarios": "Administración"
};

/**
 * Estructura común de las páginas internas: barra lateral + barra superior +
 * contenido (<Outlet/>) + pie. Reproduce el grid `.app-shell` de sihope.css.
 */
export default function AppShell() {
    const { pathname } = useLocation();
    const section = SECCIONES[pathname] ?? "SiHope";

    return (
        <div className="app-shell">
            <Sidebar />
            <Topbar section={section} />
            <main className="main">
                <Outlet />
            </main>
            <Footer />
        </div>
    );
}
