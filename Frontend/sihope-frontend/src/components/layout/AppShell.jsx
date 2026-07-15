import { useEffect, useState } from "react";
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
    const [menuOpen, setMenuOpen] = useState(false);

    useEffect(() => {
        setMenuOpen(false);
    }, [pathname]);

    return (
        <div className={`app-shell ${menuOpen ? "is-menu-open" : ""}`.trim()}>
            <Sidebar onNavigate={() => setMenuOpen(false)} />
            <button
                type="button"
                className={`sidebar-backdrop ${menuOpen ? "is-visible" : ""}`.trim()}
                aria-label="Cerrar menú"
                onClick={() => setMenuOpen(false)}
            />
            <Topbar
                section={section}
                menuOpen={menuOpen}
                onToggleMenu={() => setMenuOpen((open) => !open)}
            />
            <main className="main">
                <Outlet />
            </main>
            <Footer />
        </div>
    );
}
