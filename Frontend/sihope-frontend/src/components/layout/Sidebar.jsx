import { NavLink } from "react-router-dom";
import Isotype from "./Isotype";
import { useAuth } from "../../hooks/useAuth";
import { panelByRole } from "../../utils/roles";
import logo from "../../images/logo-sihope.jpeg";

export default function Sidebar({ onNavigate }) {
    const { user, signOut } = useAuth();
    const role = user?.rol ?? "";
    const isAdmin = role === "ADMINISTRADOR";

    const linkClass = ({ isActive }) =>
        `nav__link ${isActive ? "is-active" : ""}`.trim();

    const handleNavClick = () => {
        if (onNavigate) onNavigate();
    };

    return (
        <aside className="sidebar">
            <div className="brand-lockup">
                <Isotype src={logo} negative />
            </div>

            <nav className="nav">
                {!isAdmin && (
                    <NavLink to={panelByRole(role)} className={linkClass} end onClick={handleNavClick}>
                        <span className="nav__icon">◧</span> Panel principal
                    </NavLink>
                )}

                {isAdmin && (
                    <>
                        <NavLink to="/admin/usuarios" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◈</span> Usuarios y roles
                        </NavLink>
                        <NavLink to="/admin/asignaturas" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◨</span> Asignaturas
                        </NavLink>
                        <NavLink to="/admin/carreras" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◫</span> Carreras
                        </NavLink>
                    </>
                )}

                {role === "MONITOR" && (
                    <>
                        <NavLink to="/monitor/disponibilidad" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◷</span> Mi disponibilidad
                        </NavLink>
                        <NavLink to="/citas" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◔</span> Mis citas
                        </NavLink>
                    </>
                )}

                {role === "COORDINADOR" && (
                    <>
                        <NavLink to="/coordinador/convocatorias" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◱</span> Convocatorias
                        </NavLink>
                        <NavLink to="/coordinador/monitores" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◨</span> Asignación de materias
                        </NavLink>
                        <NavLink to="/coordinador/reportes" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◰</span> Reportes
                        </NavLink>
                    </>
                )}
                {role === "ESTUDIANTE" && (
                    <>
                        <NavLink to="/convocatorias" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◱</span> Convocatorias
                        </NavLink>
                        <NavLink to="/citas" className={linkClass} onClick={handleNavClick}>
                            <span className="nav__icon">◔</span> Mis citas
                        </NavLink>
                    </>
                )}

                <NavLink to="/monitores" className={linkClass} onClick={handleNavClick}>
                    <span className="nav__icon">◎</span> Monitores
                </NavLink>

                <NavLink to="/credenciales" className={linkClass} onClick={handleNavClick}>
                    <span className="nav__icon">⚿</span> Mis credenciales
                </NavLink>
            </nav>

            <div className="sidebar__foot">
                <button
                    type="button"
                    className="nav__link"
                    onClick={signOut}
                    style={{
                        width: "100%",
                        background: "none",
                        border: "none",
                        cursor: "pointer",
                        font: "inherit"
                    }}
                >
                    <span className="nav__icon">⇦</span> Cerrar sesión
                </button>
            </div>
        </aside>
    );
}
