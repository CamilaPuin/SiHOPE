import { NavLink } from "react-router-dom";
import Isotype from "./Isotype";
import { useAuth } from "../../hooks/useAuth";
import { panelByRole } from "../../utils/roles";
import logo from "../../images/logo-sihope.jpeg";

export default function Sidebar() {
    const { user, signOut } = useAuth();
    const role = user?.rol ?? "";
    const isAdmin = role === "ADMINISTRADOR";

    const linkClass = ({ isActive }) =>
        `nav__link ${isActive ? "is-active" : ""}`.trim();

    return (
        <aside className="sidebar">
            <div className="brand-lockup">
                <Isotype src={logo} negative />
            </div>

            <nav className="nav">
                {!isAdmin && (
                    <NavLink to={panelByRole(role)} className={linkClass} end>
                        <span className="nav__icon">◧</span> Panel principal
                    </NavLink>
                )}

                {isAdmin && (
                    <>
                        <NavLink to="/admin/usuarios" className={linkClass}>
                            <span className="nav__icon">◈</span> Usuarios y roles
                        </NavLink>
                        <NavLink to="/admin/asignaturas" className={linkClass}>
                            <span className="nav__icon">◨</span> Asignaturas
                        </NavLink>
                    </>
                )}

                {role === "MONITOR" && (
                    <>
                        <NavLink to="/monitor/disponibilidad" className={linkClass}>
                            <span className="nav__icon">◷</span> Mi disponibilidad
                        </NavLink>
                        <NavLink to="/monitor/asignaturas" className={linkClass}>
                            <span className="nav__icon">◨</span> Mis asignaturas
                        </NavLink>
                        <NavLink to="/citas" className={linkClass}>
                            <span className="nav__icon">◔</span> Mis citas
                        </NavLink>
                    </>
                )}

                {role === "COORDINADOR" && (
                    <>
                        <NavLink to="/coordinador/convocatorias" className={linkClass}>
                            <span className="nav__icon">◱</span> Convocatorias
                        </NavLink>
                        <NavLink to="/coordinador/reportes" className={linkClass}>
                            <span className="nav__icon">◰</span> Reportes
                        </NavLink>
                    </>
                )}
                {role === "ESTUDIANTE" && (
                    <>
                        <NavLink to="/convocatorias" className={linkClass}>
                            <span className="nav__icon">◱</span> Convocatorias
                        </NavLink>
                        <NavLink to="/citas" className={linkClass}>
                            <span className="nav__icon">◔</span> Mis citas
                        </NavLink>
                    </>
                )}

                <NavLink to="/monitores" className={linkClass}>
                    <span className="nav__icon">◎</span> Monitores
                </NavLink>

                <NavLink to="/credenciales" className={linkClass}>
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
