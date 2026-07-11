import { useAuth } from "../../hooks/useAuth";

export default function Topbar({ section }) {
    const { user } = useAuth();

    return (
        <header className="topbar">
            <span className="topbar__title">{section}</span>
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
