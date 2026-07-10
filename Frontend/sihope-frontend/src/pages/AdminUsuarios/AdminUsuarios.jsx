import { useEffect, useState, useCallback } from "react";
import Swal from "sweetalert2";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import {
    listar,
    crear,
    cambiarRol,
    cambiarEstado
} from "../../services/usuarioService";
import { ROLES } from "../../utils/roles";

const FORM_INICIAL = { nombre: "", correo: "", documento: "", rol: "" };

const ETIQUETA_ROL = {
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

export default function AdminUsuarios() {
    const [usuarios, setUsuarios] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [errorCarga, setErrorCarga] = useState("");

    const [form, setForm] = useState(FORM_INICIAL);
    const [errores, setErrores] = useState({});
    const [errorCrear, setErrorCrear] = useState("");
    const [exitoCrear, setExitoCrear] = useState("");
    const [creando, setCreando] = useState(false);


    const cargarUsuarios = useCallback(async () => {
        try {
            const res = await listar();
            setUsuarios(res.data ?? []);
            setErrorCarga("");
        } catch (err) {
            setErrorCarga(err.message);
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => {
        let activo = true;
        listar()
            .then((res) => {
                if (activo) setUsuarios(res.data ?? []);
            })
            .catch((err) => {
                if (activo) setErrorCarga(err.message);
            })
            .finally(() => {
                if (activo) setCargando(false);
            });
        return () => {
            activo = false;
        };
    }, []);

    const actualizar = (campo) => (e) =>
        setForm((prev) => ({ ...prev, [campo]: e.target.value }));

    const manejarCrear = async (e) => {
        e.preventDefault();
        setErrorCrear("");
        setExitoCrear("");
        setErrores({});
        setCreando(true);
        try {
            const res = await crear(form);
            setExitoCrear(
                res.message ||
                    "Usuario creado. Se enviaron las credenciales al correo institucional."
            );
            setForm(FORM_INICIAL);
            await cargarUsuarios();
        } catch (err) {
            if (err.data && typeof err.data === "object") {
                setErrores(err.data);
                setErrorCrear(
                    "Faltan campos obligatorios o hay datos inválidos. Revisa los campos resaltados."
                );
            } else {
                setErrorCrear(err.message);
            }
        } finally {
            setCreando(false);
        }
    };

    const manejarCambioRol = async (usuario, nuevoRol) => {
        if (nuevoRol === usuario.rol) return;
        try {
            await cambiarRol(usuario.id, nuevoRol);
            await cargarUsuarios();
            toast("success", "Rol actualizado. Los permisos se aplican de inmediato.");
        } catch (err) {
            toast("error", err.message);
            cargarUsuarios(); // revierte el select al estado real
        }
    };

    const manejarCambioEstado = async (usuario) => {
        const desactivar = usuario.activo;
        const confirmacion = await Swal.fire({
            title: desactivar ? "¿Desactivar cuenta?" : "¿Reactivar cuenta?",
            html: `<strong>${usuario.nombres} ${usuario.apellidos}</strong><br/>${usuario.correo}`,
            icon: desactivar ? "warning" : "question",
            showCancelButton: true,
            confirmButtonText: desactivar ? "Desactivar" : "Reactivar",
            cancelButtonText: "Cancelar",
            confirmButtonColor: desactivar ? "#a12626" : "#1f8a4c"
        });
        if (!confirmacion.isConfirmed) return;

        try {
            await cambiarEstado(usuario.id);
            await cargarUsuarios();
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

            {/* Crear usuario */}
            <section className="card" style={{ marginBottom: 28 }}>
                <div className="card__title">Crear usuario</div>
                <div className="card__subtitle">
                    Se enviarán las credenciales al correo institucional del usuario.
                </div>

                <Alert tipo="success">{exitoCrear}</Alert>
                <Alert tipo="error">{errorCrear}</Alert>

                <form onSubmit={manejarCrear} noValidate>
                    <div className="form-grid">
                        <Field
                            label="Nombre completo"
                            id="nombre"
                            value={form.nombre}
                            onChange={actualizar("nombre")}
                            placeholder="Nombre y apellidos"
                            error={errores.nombre}
                        />
                        <Field
                            label="Correo institucional"
                            id="correoU"
                            type="email"
                            value={form.correo}
                            onChange={actualizar("correo")}
                            placeholder="usuario@uptc.edu.co"
                            error={errores.correo}
                        />
                        <Field
                            label="Documento / Código"
                            id="documento"
                            value={form.documento}
                            onChange={actualizar("documento")}
                            placeholder="1053812345"
                            error={errores.documento}
                        />
                        <Field label="Rol" id="rol" error={errores.rol}>
                            <select
                                id="rol"
                                value={form.rol}
                                onChange={actualizar("rol")}
                            >
                                <option value="">Selecciona un rol…</option>
                                {ROLES.map((r) => (
                                    <option key={r} value={r}>
                                        {ETIQUETA_ROL[r]}
                                    </option>
                                ))}
                            </select>
                        </Field>
                    </div>
                    <button
                        type="submit"
                        className="btn btn-primary mt-8"
                        disabled={creando}
                    >
                        {creando ? <Spinner /> : "Crear y enviar credenciales"}
                    </button>
                </form>
            </section>

            {/* Listado de usuarios */}
            <section className="card">
                <div className="card__title">Usuarios registrados</div>
                <div className="card__subtitle">
                    Cambia el rol para actualizar permisos de inmediato o desactiva una
                    cuenta conservando su historial.
                </div>

                <Alert tipo="error">{errorCarga}</Alert>

                {cargando ? (
                    <div className="text-center mt-16">
                        <Spinner grande />
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
                                {usuarios.map((u) => (
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
                                                    manejarCambioRol(u, e.target.value)
                                                }
                                            >
                                                {ROLES.map((r) => (
                                                    <option key={r} value={r}>
                                                        {ETIQUETA_ROL[r]}
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
                                                onClick={() => manejarCambioEstado(u)}
                                            >
                                                {u.activo ? "Desactivar" : "Reactivar"}
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                {usuarios.length === 0 && (
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
