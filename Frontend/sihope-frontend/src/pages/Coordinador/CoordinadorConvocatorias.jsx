import { useEffect, useState, useCallback } from "react";
import Swal from "sweetalert2";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import {
    crearConvocatoria,
    listarTodas,
    cerrarConvocatoria,
    listarPostulaciones,
    cambiarEstadoPostulacion,
    promoverAMonitor
} from "../../services/convocatoriaService";

const FORM_INICIAL = {
    titulo: "",
    materia: "",
    requisitos: "",
    plazas: "",
    fechaLimite: "",
    descripcion: ""
};

const ETIQUETA_ESTADO = {
    PENDIENTE: "badge badge-yellow",
    APROBADA: "badge badge-active",
    RECHAZADA: "badge badge-inactive"
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

export default function CoordinadorConvocatorias() {
    const [convocatorias, setConvocatorias] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [errorCarga, setErrorCarga] = useState("");

    // Formulario de creación
    const [form, setForm] = useState(FORM_INICIAL);
    const [errores, setErrores] = useState({});
    const [errorCrear, setErrorCrear] = useState("");
    const [creando, setCreando] = useState(false);
    const [preview, setPreview] = useState(false); // modal de confirmación previa

    // Panel de postulaciones
    const [convSeleccionada, setConvSeleccionada] = useState(null);
    const [postulaciones, setPostulaciones] = useState([]);
    const [cargandoPost, setCargandoPost] = useState(false);

    const cargar = useCallback(async () => {
        try {
            const res = await listarTodas();
            setConvocatorias(res.data ?? []);
            setErrorCarga("");
        } catch (err) {
            setErrorCarga(err.message);
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => {
        cargar();
    }, [cargar]);

    const actualizar = (campo) => (e) =>
        setForm((prev) => ({ ...prev, [campo]: e.target.value }));

    // Paso 1: el coordinador revisa los datos antes de publicar (mitigación HU_008).
    const abrirPreview = (e) => {
        e.preventDefault();
        setErrorCrear("");
        setErrores({});
        setPreview(true);
    };

    // Paso 2: confirmación → se publica realmente.
    const publicar = async () => {
        setCreando(true);
        try {
            const payload = { ...form, plazas: Number(form.plazas) };
            const res = await crearConvocatoria(payload);
            setPreview(false);
            setForm(FORM_INICIAL);
            await cargar();
            toast("success", res.message || "Convocatoria publicada.");
        } catch (err) {
            setPreview(false);
            if (err.data && typeof err.data === "object" && !Array.isArray(err.data)) {
                setErrores(err.data);
                setErrorCrear("Revisa los campos resaltados.");
            } else {
                setErrorCrear(err.message);
            }
        } finally {
            setCreando(false);
        }
    };

    const cerrar = async (c) => {
        const confirmacion = await Swal.fire({
            title: "¿Cerrar convocatoria?",
            html: `<strong>${c.titulo}</strong><br/>Dejará de ser visible para los aspirantes.`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Cerrar",
            cancelButtonText: "Cancelar",
            confirmButtonColor: "#a12626"
        });
        if (!confirmacion.isConfirmed) return;
        try {
            await cerrarConvocatoria(c.id);
            await cargar();
            toast("success", "Convocatoria cerrada.");
        } catch (err) {
            toast("error", err.message);
        }
    };

    const verPostulaciones = async (c) => {
        setConvSeleccionada(c);
        setCargandoPost(true);
        try {
            const res = await listarPostulaciones(c.id);
            setPostulaciones(res.data ?? []);
        } catch (err) {
            toast("error", err.message);
            setPostulaciones([]);
        } finally {
            setCargandoPost(false);
        }
    };

    const recargarPostulaciones = async () => {
        if (!convSeleccionada) return;
        const res = await listarPostulaciones(convSeleccionada.id);
        setPostulaciones(res.data ?? []);
    };

    const decidir = async (p, estado) => {
        try {
            await cambiarEstadoPostulacion(p.id, estado);
            await recargarPostulaciones();
            toast("success", estado === "APROBADA" ? "Postulación aprobada." : "Postulación rechazada.");
        } catch (err) {
            toast("error", err.message);
        }
    };

    const promover = async (p) => {
        const confirmacion = await Swal.fire({
            title: "¿Asignar como monitor?",
            html: `<strong>${p.aspiranteNombre}</strong><br/>${p.aspiranteCorreo}<br/><br/>
                   Se le otorgará el rol MONITOR. Su sesión actual se invalidará y deberá
                   iniciar sesión de nuevo para aplicar los permisos.`,
            icon: "question",
            showCancelButton: true,
            confirmButtonText: "Asignar monitor",
            cancelButtonText: "Cancelar",
            confirmButtonColor: "#1f8a4c"
        });
        if (!confirmacion.isConfirmed) return;
        try {
            const res = await promoverAMonitor(p.id);
            await recargarPostulaciones();
            Swal.fire({
                icon: "success",
                title: "Monitor asignado",
                text: res.message,
                confirmButtonColor: "#e0a80d"
            });
        } catch (err) {
            toast("error", err.message);
        }
    };

    return (
        <>
            <div className="page-head">
                <h1>Convocatorias</h1>
                <p>
                    Crea y publica convocatorias de selección, revisa las postulaciones y
                    promueve a los aspirantes aprobados a monitor.
                </p>
            </div>

            {/* Crear convocatoria */}
            <section className="card" style={{ marginBottom: 28 }}>
                <div className="card__title">Nueva convocatoria</div>
                <div className="card__subtitle">
                    Revisa los datos en la confirmación antes de publicar.
                </div>

                <Alert tipo="error">{errorCrear}</Alert>

                <form onSubmit={abrirPreview} noValidate>
                    <div className="form-grid">
                        <Field
                            label="Título"
                            id="titulo"
                            value={form.titulo}
                            onChange={actualizar("titulo")}
                            placeholder="Monitor de Estructuras de Datos"
                            error={errores.titulo}
                        />
                        <Field
                            label="Materia"
                            id="materia"
                            value={form.materia}
                            onChange={actualizar("materia")}
                            placeholder="Estructuras de Datos"
                            error={errores.materia}
                        />
                        <Field
                            label="Plazas"
                            id="plazas"
                            type="number"
                            min="1"
                            value={form.plazas}
                            onChange={actualizar("plazas")}
                            placeholder="2"
                            error={errores.plazas}
                        />
                        <Field
                            label="Fecha límite"
                            id="fechaLimite"
                            type="date"
                            value={form.fechaLimite}
                            onChange={actualizar("fechaLimite")}
                            error={errores.fechaLimite}
                        />
                    </div>
                    <Field label="Requisitos" id="requisitos" error={errores.requisitos}>
                        <textarea
                            id="requisitos"
                            rows={2}
                            value={form.requisitos}
                            onChange={actualizar("requisitos")}
                            placeholder="Haber aprobado la materia con nota mínima 4.0, promedio ≥ 3.8…"
                        />
                    </Field>
                    <Field label="Descripción (opcional)" id="descripcion">
                        <textarea
                            id="descripcion"
                            rows={2}
                            value={form.descripcion}
                            onChange={actualizar("descripcion")}
                            placeholder="Detalles adicionales de la convocatoria…"
                        />
                    </Field>
                    <button type="submit" className="btn btn-primary mt-8">
                        Revisar y publicar
                    </button>
                </form>
            </section>

            {/* Listado */}
            <section className="card">
                <div className="card__title">Convocatorias del periodo</div>
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
                                    <th>Título</th>
                                    <th>Materia</th>
                                    <th>Plazas</th>
                                    <th>Fecha límite</th>
                                    <th>Estado</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                {convocatorias.map((c) => (
                                    <tr key={c.id}>
                                        <td><strong>{c.titulo}</strong></td>
                                        <td>{c.materia}</td>
                                        <td>{c.plazas}</td>
                                        <td>{c.fechaLimite}</td>
                                        <td>
                                            <span
                                                className={
                                                    c.estado === "ABIERTA"
                                                        ? "badge badge-active"
                                                        : "badge badge-inactive"
                                                }
                                            >
                                                {c.estado}
                                            </span>
                                        </td>
                                        <td>
                                            <button
                                                type="button"
                                                className="btn btn-ghost btn-sm"
                                                onClick={() => verPostulaciones(c)}
                                            >
                                                Postulaciones
                                            </button>{" "}
                                            {c.estado === "ABIERTA" && (
                                                <button
                                                    type="button"
                                                    className="btn btn-danger btn-sm"
                                                    onClick={() => cerrar(c)}
                                                >
                                                    Cerrar
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                                {convocatorias.length === 0 && (
                                    <tr>
                                        <td colSpan={6} className="text-center muted">
                                            Aún no has creado convocatorias.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            {/* Modal de confirmación previa a la publicación (mitigación HU_008) */}
            {preview && (
                <div className="modal-overlay" onClick={() => !creando && setPreview(false)}>
                    <div className="modal-box card" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
                        <div className="card__title">Confirmar publicación</div>
                        <div className="card__subtitle">
                            Verifica los datos. Una vez publicada, la convocatoria quedará
                            visible para los aspirantes.
                        </div>
                        <div className="schedule">
                            <div className="schedule__row"><span>Título</span><span><strong>{form.titulo || "—"}</strong></span></div>
                            <div className="schedule__row"><span>Materia</span><span>{form.materia || "—"}</span></div>
                            <div className="schedule__row"><span>Plazas</span><span>{form.plazas || "—"}</span></div>
                            <div className="schedule__row"><span>Fecha límite</span><span>{form.fechaLimite || "—"}</span></div>
                        </div>
                        {form.requisitos && (
                            <p className="muted" style={{ marginTop: 12 }}>
                                <strong>Requisitos:</strong> {form.requisitos}
                            </p>
                        )}
                        <div className="modal-actions">
                            <button type="button" className="btn btn-ghost" onClick={() => setPreview(false)} disabled={creando}>
                                Volver a editar
                            </button>
                            <button type="button" className="btn btn-primary" onClick={publicar} disabled={creando}>
                                {creando ? <Spinner /> : "Publicar convocatoria"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Panel de postulaciones (HU_009) */}
            {convSeleccionada && (
                <div className="modal-overlay" onClick={() => setConvSeleccionada(null)}>
                    <div className="modal-box card" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
                        <div className="card__title">Postulaciones · {convSeleccionada.titulo}</div>
                        <div className="card__subtitle">
                            Aprueba o rechaza aspirantes; los aprobados pueden asignarse como
                            monitor.
                        </div>

                        {cargandoPost ? (
                            <div className="text-center mt-16"><Spinner grande /></div>
                        ) : postulaciones.length === 0 ? (
                            <p className="muted mt-16">Esta convocatoria aún no tiene postulaciones.</p>
                        ) : (
                            <div className="schedule">
                                {postulaciones.map((p) => (
                                    <div
                                        key={p.id}
                                        className="schedule__row"
                                        style={{ flexDirection: "column", alignItems: "stretch", gap: 8 }}
                                    >
                                        <div style={{ display: "flex", justifyContent: "space-between", gap: 12 }}>
                                            <span>
                                                <strong>{p.aspiranteNombre}</strong>
                                                <br />
                                                <span className="muted" style={{ fontSize: "0.82rem" }}>
                                                    {p.aspiranteCorreo}
                                                </span>
                                            </span>
                                            <span className={ETIQUETA_ESTADO[p.estado] ?? "badge"}>
                                                {p.estado}
                                            </span>
                                        </div>

                                        {p.datos && Object.keys(p.datos).length > 0 && (
                                            <div className="muted" style={{ fontSize: "0.85rem" }}>
                                                {Object.entries(p.datos).map(([k, v]) => (
                                                    <div key={k}>
                                                        <strong>{k}:</strong> {v}
                                                    </div>
                                                ))}
                                            </div>
                                        )}

                                        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                                            {p.estado !== "APROBADA" && (
                                                <button
                                                    type="button"
                                                    className="btn btn-ghost btn-sm"
                                                    onClick={() => decidir(p, "APROBADA")}
                                                >
                                                    Aprobar
                                                </button>
                                            )}
                                            {p.estado !== "RECHAZADA" && (
                                                <button
                                                    type="button"
                                                    className="btn btn-danger btn-sm"
                                                    onClick={() => decidir(p, "RECHAZADA")}
                                                >
                                                    Rechazar
                                                </button>
                                            )}
                                            {p.estado === "APROBADA" && (
                                                <button
                                                    type="button"
                                                    className="btn btn-primary btn-sm"
                                                    onClick={() => promover(p)}
                                                >
                                                    Asignar como monitor
                                                </button>
                                            )}
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}

                        <div className="modal-actions">
                            <button type="button" className="btn btn-ghost" onClick={() => setConvSeleccionada(null)}>
                                Cerrar
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}
