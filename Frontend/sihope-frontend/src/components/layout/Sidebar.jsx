import { NavLink } from "react-router-dom";
import Isotipo, { Wordmark } from "./Isotipo";
import { useAuth } from "../../hooks/useAuth";
import { panelPorRol } from "../../utils/roles";

/**
 * Barra lateral de navegación. Los enlaces se muestran según el rol del usuario
 * en sesión, replicando la lógica del fragmento `sidebar` de layout.html.
 */
export default function Sidebar() {
    const { usuario, cerrarSesion } = useAuth();
    const rol = usuario?.rol ?? "";
    const esAdmin = rol === "ADMINISTRADOR";

    const claseLink = ({ isActive }) =>
        `nav__link ${isActive ? "is-active" : ""}`.trim();

    return (
        <aside className="sidebar">
            <div className="brand-lockup">
                <Isotipo negativo />
                <Wordmark negativo />
            </div>

            <nav className="nav">
                {/* Panel principal: estudiante, monitor y coordinador */}
                {!esAdmin && (
                    <NavLink to={panelPorRol(rol)} className={claseLink}>
                        <span className="nav__icon">◧</span> Panel principal
                    </NavLink>
                )}

                {/* Usuarios y roles: solo administrador */}
                {esAdmin && (
                    <NavLink to="/admin/usuarios" className={claseLink}>
                        <span className="nav__icon">◈</span> Usuarios y roles
                    </NavLink>
                )}

                {/* Mi disponibilidad: solo monitor (HU_006) */}
                {rol === "MONITOR" && (
                    <NavLink to="/monitor/disponibilidad" className={claseLink}>
                        <span className="nav__icon">◷</span> Mi disponibilidad
                    </NavLink>
                )}

                {/* Convocatorias: gestión (coordinador) o postulación (estudiante) */}
                {rol === "COORDINADOR" && (
                    <NavLink to="/coordinador/convocatorias" className={claseLink}>
                        <span className="nav__icon">◱</span> Convocatorias
                    </NavLink>
                )}
                {rol === "ESTUDIANTE" && (
                    <NavLink to="/convocatorias" className={claseLink}>
                        <span className="nav__icon">◱</span> Convocatorias
                    </NavLink>
                )}

                {/* Monitores: disponible para todos los roles */}
                <NavLink to="/monitores" className={claseLink}>
                    <span className="nav__icon">◎</span> Monitores
                </NavLink>

                {/* Credenciales: disponible para todos los roles */}
                <NavLink to="/credenciales" className={claseLink}>
                    <span className="nav__icon">⚿</span> Mis credenciales
                </NavLink>
            </nav>

            <div className="sidebar__foot">
                <button
                    type="button"
                    className="nav__link"
                    onClick={cerrarSesion}
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
