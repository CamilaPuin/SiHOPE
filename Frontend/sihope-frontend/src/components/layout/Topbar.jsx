import { useAuth } from "../../hooks/useAuth";

/**
 * Barra superior. Muestra el título de la sección y los datos del usuario en
 * sesión (rol, iniciales y nombre). Portado del fragmento `topbar` de layout.html.
 */
export default function Topbar({ section }) {
    const { usuario } = useAuth();

    return (
        <header className="topbar">
            <span className="topbar__title">{section}</span>
            {usuario && (
                <div className="topbar__user">
                    <span className="badge badge-role">{usuario.rol}</span>
                    <span className="avatar">{usuario.iniciales}</span>
                    <span className="topbar__user-name">{usuario.nombre}</span>
                </div>
            )}
        </header>
    );
}
