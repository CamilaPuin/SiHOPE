import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { panelByRole } from "../utils/roles";

export default function RoleRoute({ role }) {
    const { user } = useAuth();
    const allowed = Array.isArray(role) ? role : [role];

    if (!user || !allowed.includes(user.rol)) {
        return <Navigate to={panelByRole(user?.rol)} replace />;
    }

    return <Outlet />;
}
