import { useEffect, useState, useCallback } from "react";
import Swal from "sweetalert2";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import {
    list,
    create,
    changeRole,
    changeStatus
} from "../../services/userService";
import { ROLES } from "../../utils/roles";

const INITIAL_FORM = { nombre: "", correo: "", documento: "", rol: "" };

const ROLE_LABEL = {
    ESTUDIANTE: "Estudiante",
    MONITOR: "Monitor",
    COORDINADOR: "Coordinador",
    ADMINISTRADOR: "Administrador"
};

const toast = (icon, title) =>
    Swal.fire({
        toast: true,
        position: "top-end",
        showConfirmButton: false,
        timer: 2600,
        timerProgressBar: true,
        icon,
        title
    });

export default function AdminUsers() {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [form, setForm] = useState(INITIAL_FORM);
    const [errors, setErrors] = useState({});
    const [createError, setCreateError] = useState("");
    const [createSuccess, setCreateSuccess] = useState("");
    const [creating, setCreating] = useState(false);

    const loadUsers = useCallback(async () => {
        try {
            const res = await list();
            setUsers(res.data ?? []);
            setLoadError("");
        } catch (err) {
            setLoadError(err.message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        let active = true;
        list()
            .then((res) => {
                if (active) setUsers(res.data ?? []);
            })
            .catch((err) => {
                if (active) setLoadError(err.message);
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, []);

    const update = (field) => (e) =>
        setForm((prev) => ({ ...prev, [field]: e.target.value }));

    const handleCreate = async (e) => {
        e.preventDefault();
        setCreateError("");
        setCreateSuccess("");
        setErrors({});
        setCreating(true);
        try {
            const res = await create(form);
            setCreateSuccess(
                res.message ||
                    "Usuario creado. Se enviaron las credenciales al correo institucional."
            );
            setForm(INITIAL_FORM);
            await loadUsers();
        } catch (err) {
            if (err.data && typeof err.data === "object") {
                setErrors(err.data);
                setCreateError(
                    "Faltan campos obligatorios o hay datos inválidos. Revisa los campos resaltados."
                );
            } else {
                setCreateError(err.message);
            }
        } finally {
            setCreating(false);
        }
    };

    const handleRoleChange = async (user, newRole) => {
        if (newRole === user.rol) return;
        try {
            await changeRole(user.id, newRole);
            await loadUsers();
            toast("success", "Rol actualizado. Los permisos se aplican de inmediato.");
        } catch (err) {
            toast("error", err.message);
            loadUsers();
        }
    };

    const handleStatusChange = async (user) => {
        const deactivate = user.activo;
        const confirmation = await Swal.fire({
            title: deactivate ? "¿Desactivar cuenta?" : "¿Reactivar cuenta?",
            html: `<strong>${user.nombres} ${user.apellidos}</strong><br/>${user.correo}`,
            icon: deactivate ? "warning" : "question",
            showCancelButton: true,
            confirmButtonText: deactivate ? "Desactivar" : "Reactivar",
            cancelButtonText: "Cancelar",
            confirmButtonColor: deactivate ? "#a12626" : "#1f8a4c"
        });
        if (!confirmation.isConfirmed) return;

        try {
            await changeStatus(user.id);
            await loadUsers();
            toast("success", "Estado de la cuenta actualizado.");
        } catch (err) {
            toast("error", err.message);
        }
    };

    return (
        <>
            <div className="page-head">
                <h1>Usuarios y roles</h1>
                <p>
                    Crea cuentas, asigna roles y gestiona el acceso de los usuarios del
                    sistema.
                </p>
            </div>

            <section className="card" style={{ marginBottom: 28 }}>
                <div className="card__title">Crear usuario</div>
                <div className="card__subtitle">
                    Se enviarán las credenciales al correo institucional del usuario.
                </div>

                <Alert type="success">{createSuccess}</Alert>
                <Alert type="error">{createError}</Alert>

                <form onSubmit={handleCreate} noValidate>
                    <div className="form-grid">
                        <Field
                            label="Nombre completo"
                            id="nombre"
                            value={form.nombre}
                            onChange={update("nombre")}
                            placeholder="Nombre y apellidos"
                            error={errors.nombre}
                        />
                        <Field
                            label="Correo institucional"
                            id="correoU"
                            type="email"
                            value={form.correo}
                            onChange={update("correo")}
                            placeholder="usuario@uptc.edu.co"
                            error={errors.correo}
                        />
                        <Field
                            label="Documento / Código"
                            id="documento"
                            value={form.documento}
                            onChange={update("documento")}
                            placeholder="1053812345"
                            error={errors.documento}
                        />
                        <Field label="Rol" id="rol" error={errors.rol}>
                            <select
                                id="rol"
                                value={form.rol}
                                onChange={update("rol")}
                            >
                                <option value="">Selecciona un rol…</option>
                                {ROLES.map((r) => (
                                    <option key={r} value={r}>
                                        {ROLE_LABEL[r]}
                                    </option>
                                ))}
                            </select>
                        </Field>
                    </div>
                    <button
                        type="submit"
                        className="btn btn-primary mt-8"
                        disabled={creating}
                    >
                        {creating ? <Spinner /> : "Crear y enviar credenciales"}
                    </button>
                </form>
            </section>

            <section className="card">
                <div className="card__title">Usuarios registrados</div>
                <div className="card__subtitle">
                    Cambia el rol para actualizar permisos de inmediato o desactiva una
                    cuenta conservando su historial.
                </div>

                <Alert type="error">{loadError}</Alert>

                {loading ? (
                    <div className="text-center mt-16">
                        <Spinner large />
                    </div>
                ) : (
                    <div className="table-wrap">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Usuario</th>
                                    <th>Correo</th>
                                    <th>Rol</th>
                                    <th>Estado</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                {users.map((u) => (
                                    <tr key={u.id} className={u.activo ? "" : "is-inactive"}>
                                        <td>
                                            <strong>
                                                {u.nombres} {u.apellidos}
                                            </strong>
                                            <br />
                                            <span
                                                className="muted"
                                                style={{ fontSize: "0.82rem" }}
                                            >
                                                {u.codigo}
                                            </span>
                                        </td>
                                        <td>{u.correo}</td>
                                        <td>
                                            <select
                                                value={u.rol}
                                                aria-label="Rol"
                                                onChange={(e) =>
                                                    handleRoleChange(u, e.target.value)
                                                }
                                            >
                                                {ROLES.map((r) => (
                                                    <option key={r} value={r}>
                                                        {ROLE_LABEL[r]}
                                                    </option>
                                                ))}
                                            </select>
                                        </td>
                                        <td>
                                            {u.activo ? (
                                                <span className="badge badge-active">
                                                    Activo
                                                </span>
                                            ) : (
                                                <span className="badge badge-inactive">
                                                    Inactivo
                                                </span>
                                            )}
                                        </td>
                                        <td>
                                            <button
                                                type="button"
                                                className={`btn btn-sm ${
                                                    u.activo ? "btn-danger" : "btn-ghost"
                                                }`}
                                                onClick={() => handleStatusChange(u)}
                                            >
                                                {u.activo ? "Desactivar" : "Reactivar"}
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                {users.length === 0 && (
                                    <tr>
                                        <td
                                            colSpan={5}
                                            className="text-center muted"
                                        >
                                            No hay usuarios registrados.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                )}

                <p className="muted mt-16" style={{ fontSize: "0.85rem" }}>
                    Cada cambio de rol y desactivación queda registrado en el historial del
                    usuario.
                </p>
            </section>
        </>
    );
}
