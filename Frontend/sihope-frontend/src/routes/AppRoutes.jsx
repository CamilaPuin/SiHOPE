import { Routes, Route, Navigate } from "react-router-dom";

import ProtectedRoute from "../components/ProtectedRoute";
import RoleRoute from "../components/RoleRoute";
import AppShell from "../components/layout/AppShell";
import { useAuth } from "../hooks/useAuth";
import { panelByRole } from "../utils/roles";
import { PageLoader } from "../components/common/Spinner";

import Login from "../pages/Login/Login";
import Registration from "../pages/Registration/Registration";
import RecoverPassword from "../pages/RecoverPassword/RecoverPassword";
import ResetPassword from "../pages/ResetPassword/ResetPassword";
import Verify from "../pages/Verify/Verify";

import Home from "../pages/Home/Home";
import Coordinator from "../pages/Coordinator/Coordinator";
import CoordinatorVacancies from "../pages/Coordinator/CoordinatorVacancies";
import CoordinatorMonitors from "../pages/Coordinator/CoordinatorMonitors";
import CoordinatorReports from "../pages/Coordinator/CoordinatorReports";
import Monitor from "../pages/Monitor/Monitor";
import MonitorAvailability from "../pages/Monitor/MonitorAvailability";
import Monitors from "../pages/Monitors/Monitors";
import Vacancies from "../pages/Vacancies/Vacancies";
import Credentials from "../pages/Credentials/Credentials";
import AdminUsers from "../pages/AdminUsers/AdminUsers";
import AdminSubjects from "../pages/AdminSubjects/AdminSubjects";
import AdminCareers from "../pages/AdminCareers/AdminCareers";
import BookAppointment from "../pages/Appointments/BookAppointment";
import MyAppointments from "../pages/Appointments/MyAppointments";

function Root() {
    const { user, loading } = useAuth();
    if (loading) return <PageLoader message="Cargando…" />;
    if (!user) return <Navigate to="/login" replace />;
    return <Navigate to={panelByRole(user.rol)} replace />;
}

export default function AppRoutes() {
    return (
        <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/registro" element={<Registration />} />
            <Route path="/recuperar" element={<RecoverPassword />} />
            <Route path="/restablecer" element={<ResetPassword />} />
            <Route path="/verificar" element={<Verify />} />

            <Route element={<ProtectedRoute />}>
                <Route element={<AppShell />}>
                    <Route path="/home" element={<Home />} />
                    <Route path="/coordinador" element={<Coordinator />} />
                    <Route path="/monitor" element={<Monitor />} />
                    <Route path="/monitores" element={<Monitors />} />
                    <Route path="/convocatorias" element={<Vacancies />} />
                    <Route path="/credenciales" element={<Credentials />} />
                    <Route path="/citas" element={<MyAppointments />} />
                    <Route path="/agendar/:monitorId" element={<BookAppointment />} />

                    <Route element={<RoleRoute role="MONITOR" />}>
                        <Route path="/monitor/disponibilidad" element={<MonitorAvailability />} />
                    </Route>

                    <Route element={<RoleRoute role="COORDINADOR" />}>
                        <Route path="/coordinador/convocatorias" element={<CoordinatorVacancies />} />
                        <Route path="/coordinador/monitores" element={<CoordinatorMonitors />} />
                        <Route path="/coordinador/reportes" element={<CoordinatorReports />} />
                    </Route>

                    <Route element={<RoleRoute role="ADMINISTRADOR" />}>
                        <Route path="/admin/usuarios" element={<AdminUsers />} />
                        <Route path="/admin/asignaturas" element={<AdminSubjects />} />
                        <Route path="/admin/carreras" element={<AdminCareers />} />
                    </Route>
                </Route>
            </Route>

            <Route path="/" element={<Root />} />
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
    );
}
