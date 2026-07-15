import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import { panelByRole } from "../../utils/roles";

export default function Topbar({ section, menuOpen, onToggleMenu }) {
    const { user } = useAuth();
    const navigate = useNavigate();
    const { pathname } = useLocation();

    const landing = user ? panelByRole(user.rol) : "/home";
    const showBack = pathname !== landing && pathname !== "/home";

    return (
        <header className="topbar">
            <div className="topbar__lead">
                <button
                    type="button"
                    className="topbar__menu-btn"
                    aria-label={menuOpen ? "Cerrar menú" : "Abrir menú"}
                    aria-expanded={menuOpen}
                    onClick={onToggleMenu}
                >
                    <span className="topbar__menu-icon" aria-hidden="true">
                        {menuOpen ? "✕" : "☰"}
                    </span>
                </button>
                {showBack && (
                    <button
                        type="button"
                        className="topbar__back-btn"
                        aria-label="Volver atrás"
                        onClick={() => navigate(-1)}
                    >
                        <svg
                            className="topbar__back-icon"
                            viewBox="0 0 24 24"
                            width="26"
                            height="26"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            aria-hidden="true"
                        >
                            <circle cx="12" cy="12" r="10" />
                            <polyline points="13.5 8 9.5 12 13.5 16" />
                        </svg>
                    </button>
                )}
                <span className="topbar__title">{section}</span>
            </div>
            {user && (
                <div className="topbar__user">
                    <span className="badge badge-role">{user.rol}</span>
                    <span className="avatar">{user.iniciales}</span>
                    <span className="topbar__user-name">{user.nombre}</span>
                </div>
            )}
        </header>
    );
}
