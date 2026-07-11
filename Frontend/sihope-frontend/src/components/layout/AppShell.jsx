import { Outlet, useLocation } from "react-router-dom";
import Sidebar from "./Sidebar";
import Topbar from "./Topbar";
import Footer from "./Footer";

const SECTIONS = {
    "/home": "Panel principal",
    "/coordinador": "Panel del coordinador",
    "/monitor": "Panel del monitor",
    "/monitores": "Monitores",
    "/credenciales": "Mi cuenta",
    "/admin/usuarios": "Administración"
};

export default function AppShell() {
    const { pathname } = useLocation();
    const section = SECTIONS[pathname] ?? "SiHope";

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
