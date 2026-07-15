import { useEffect, useState, useCallback, useMemo } from "react";
import Swal from "sweetalert2";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import {
    createVacancy,
    listAll,
    closeVacancy,
    listApplications,
    changeApplicationStatus,
    promoteToMonitor
} from "../../services/vacancyService";
import { listAsignaturas } from "../../services/asignaturaService";

const TODAY = new Date().toLocaleDateString("en-CA");

const INITIAL_FORM = {
    titulo: "",
    materiaIds: [],
    requisitos: "",
    plazas: "",
    fechaLimite: "",
    descripcion: ""
};

const STATUS_BADGE = {
    PENDIENTE: "badge badge-yellow",
    APROBADA: "badge badge-active",
    RECHAZADA: "badge badge-inactive",
    MONITOR_ASIGNADO: "badge badge-active"
};

const STATUS_LABEL = {
    MONITOR_ASIGNADO: "MONITOR ASIGNADO"
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

export default function CoordinatorVacancies() {
    const [vacancies, setVacancies] = useState([]);
    const [catalog, setCatalog] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [form, setForm] = useState(INITIAL_FORM);
    const [errors, setErrors] = useState({});
    const [createError, setCreateError] = useState("");
    const [creating, setCreating] = useState(false);
    const [preview, setPreview] = useState(false);

    const [selectedVacancy, setSelectedVacancy] = useState(null);
    const [applications, setApplications] = useState([]);
    const [loadingApplications, setLoadingApplications] = useState(false);
    const load = useCallback(async () => {
        try {
            const res = await listAll();
            setVacancies(res.data ?? []);
            setLoadError("");
        } catch (err) {
            setLoadError(err.message);
        }
    }, []);

    useEffect(() => {
        let active = true;
        Promise.all([listAll(), listAsignaturas()])
            .then(([vacanciesRes, catalogRes]) => {
                if (active) {
                    setVacancies(vacanciesRes.data ?? []);
                    setCatalog(catalogRes.data ?? []);
                    setLoadError("");
                }
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

    const toggleMateria = (id) =>
        setForm((prev) => ({
            ...prev,
            materiaIds: prev.materiaIds.includes(id)
                ? prev.materiaIds.filter((m) => m !== id)
                : [...prev.materiaIds, id]
        }));

    const materiaNames = useMemo(
        () =>
            catalog
                .filter((a) => form.materiaIds.includes(a.id))
                .map((a) => a.nombre),
        [catalog, form.materiaIds]
    );

    const openPreview = (e) => {
        e.preventDefault();
        setCreateError("");
        setErrors({});
        setPreview(true);
    };

    const publish = async () => {
        setCreating(true);
        try {
            const payload = { ...form, plazas: Number(form.plazas) };
            const res = await createVacancy(payload);
            setPreview(false);
            setForm(INITIAL_FORM);
            await load();
            toast("success", res.message || "Convocatoria publicada.");
        } catch (err) {
            setPreview(false);
            if (err.data && typeof err.data === "object" && !Array.isArray(err.data)) {
                setErrors(err.data);
                setCreateError("Revisa los campos resaltados.");
            } else {
                setCreateError(err.message);
            }
        } finally {
            setCreating(false);
        }
    };

    const close = async (c) => {
        const confirmation = await Swal.fire({
            title: "¿Cerrar convocatoria?",
            html: `<strong>${c.titulo}</strong><br/>Dejará de ser visible para los aspirantes.`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Cerrar",
            cancelButtonText: "Cancelar",
            confirmButtonColor: "#a12626"
        });
        if (!confirmation.isConfirmed) return;
        try {
            await closeVacancy(c.id);
            await load();
            toast("success", "Convocatoria cerrada.");
        } catch (err) {
            toast("error", err.message);
        }
    };

    const viewApplications = async (c) => {
        setSelectedVacancy(c);
        setLoadingApplications(true);
        try {
            const res = await listApplications(c.id);
            setApplications(res.data ?? []);
        } catch (err) {
            toast("error", err.message);
            setApplications([]);
        } finally {
            setLoadingApplications(false);
        }
    };

    const reloadApplications = async () => {
        if (!selectedVacancy) return;
        const res = await listApplications(selectedVacancy.id);
        setApplications(res.data ?? []);
    };

    const decide = async (p, estado) => {
        try {
            await changeApplicationStatus(p.id, estado);
            await reloadApplications();
            toast("success", estado === "APROBADA" ? "Postulación aprobada." : "Postulación rechazada.");
        } catch (err) {
            toast("error", err.message);
        }
    };

    const promote = async (p) => {
        const confirmation = await Swal.fire({
            title: "¿Asignar como monitor?",
            html: `<strong>${p.aspiranteNombre}</strong><br/>${p.aspiranteCorreo}<br/><br/>
                   Se le otorgará el rol MONITOR y se le asignarán las materias de esta
                   convocatoria. Su sesión actual se invalidará y deberá iniciar sesión
                   de nuevo para aplicar los permisos.`,
            icon: "question",
            showCancelButton: true,
            confirmButtonText: "Asignar monitor",
            cancelButtonText: "Cancelar",
            confirmButtonColor: "#1f8a4c"
        });
        if (!confirmation.isConfirmed) return;
        try {
            const res = await promoteToMonitor(p.id);
            await reloadApplications();
            await load();
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

            <section className="card" style={{ marginBottom: 28 }}>
                <div className="card__title">Nueva convocatoria</div>
                <div className="card__subtitle">
                    Revisa los datos en la confirmación antes de publicar.
                </div>

                <Alert type="error">{createError}</Alert>

                <form onSubmit={openPreview} noValidate>
                    <div className="form-grid">
                        <Field
                            label="Título"
                            id="titulo"
                            value={form.titulo}
                            onChange={update("titulo")}
                            placeholder="Monitor de Estructuras de Datos"
                            error={errors.titulo}
                        />
                        <Field
                            label="Plazas"
                            id="plazas"
                            type="number"
                            min="1"
                            value={form.plazas}
                            onChange={update("plazas")}
                            placeholder="2"
                            error={errors.plazas}
                        />
                        <Field
                            label="Fecha límite"
                            id="fechaLimite"
                            type="date"
                            min={TODAY}
                            value={form.fechaLimite}
                            onChange={update("fechaLimite")}
                            error={errors.fechaLimite}
                        />
                    </div>
                    <Field
                        label="Materias que orientará el monitor"
                        id="materias"
                        hint="del catálogo del administrador"
                        error={errors.materiaIds}
                    >
                        {catalog.length === 0 ? (
                            <p className="muted">
                                El catálogo de asignaturas está vacío. Pide al administrador que
                                las registre antes de crear la convocatoria.
                            </p>
                        ) : (
                            <div className="chips">
                                {catalog.map((a) => (
                                    <label
                                        key={a.id}
                                        className="chip"
                                        style={{ cursor: "pointer" }}
                                    >
                                        <input
                                            type="checkbox"
                                            checked={form.materiaIds.includes(a.id)}
                                            onChange={() => toggleMateria(a.id)}
                                            style={{ marginRight: 6 }}
                                        />
                                        {a.nombre}
                                    </label>
                                ))}
                            </div>
                        )}
                    </Field>
                    <Field label="Requisitos" id="requisitos" error={errors.requisitos}>
                        <textarea
                            id="requisitos"
                            rows={2}
                            value={form.requisitos}
                            onChange={update("requisitos")}
                            placeholder="Haber aprobado la materia con nota mínima 4.0, promedio ≥ 3.8…"
                        />
                    </Field>
                    <Field label="Descripción (opcional)" id="descripcion">
                        <textarea
                            id="descripcion"
                            rows={2}
                            value={form.descripcion}
                            onChange={update("descripcion")}
                            placeholder="Detalles adicionales de la convocatoria…"
                        />
                    </Field>
                    <button type="submit" className="btn btn-primary mt-8">
                        Revisar y publicar
                    </button>
                </form>
            </section>

            <section className="card">
                <div className="card__title">Convocatorias del periodo</div>
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
                                    <th>Título</th>
                                    <th>Materia</th>
                                    <th>Plazas</th>
                                    <th>Fecha límite</th>
                                    <th>Estado</th>
                                    <th>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                {vacancies.map((c) => (
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
                                                onClick={() => viewApplications(c)}
                                            >
                                                Postulaciones
                                            </button>{" "}
                                            {c.estado === "ABIERTA" && (
                                                <button
                                                    type="button"
                                                    className="btn btn-danger btn-sm"
                                                    onClick={() => close(c)}
                                                >
                                                    Cerrar
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))}
                                {vacancies.length === 0 && (
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

            {preview && (
                <div className="modal-overlay" onClick={() => !creating && setPreview(false)}>
                    <div className="modal-box card" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
                        <div className="card__title">Confirmar publicación</div>
                        <div className="card__subtitle">
                            Verifica los datos. Una vez publicada, la convocatoria quedará
                            visible para los aspirantes.
                        </div>
                        <div className="schedule">
                            <div className="schedule__row"><span>Título</span><span><strong>{form.titulo || "-"}</strong></span></div>
                            <div className="schedule__row"><span>Materias</span><span>{materiaNames.length > 0 ? materiaNames.join(", ") : "-"}</span></div>
                            <div className="schedule__row"><span>Plazas</span><span>{form.plazas || "-"}</span></div>
                            <div className="schedule__row"><span>Fecha límite</span><span>{form.fechaLimite || "-"}</span></div>
                        </div>
                        {form.requisitos && (
                            <p className="muted" style={{ marginTop: 12 }}>
                                <strong>Requisitos:</strong> {form.requisitos}
                            </p>
                        )}
                        <div className="modal-actions">
                            <button type="button" className="btn btn-ghost" onClick={() => setPreview(false)} disabled={creating}>
                                Volver a editar
                            </button>
                            <button type="button" className="btn btn-primary" onClick={publish} disabled={creating}>
                                {creating ? <Spinner /> : "Publicar convocatoria"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {selectedVacancy && (() => {
                const assignedCount = applications.filter(
                    (a) => a.estado === "MONITOR_ASIGNADO"
                ).length;
                const slotsFull = assignedCount >= selectedVacancy.plazas;
                return (
                <div className="modal-overlay" onClick={() => setSelectedVacancy(null)}>
                    <div className="modal-box card" onClick={(e) => e.stopPropagation()} role="dialog" aria-modal="true">
                        <div className="card__title">Postulaciones · {selectedVacancy.titulo}</div>
                        <div className="card__subtitle">
                            Aprueba o rechaza aspirantes; los aprobados pueden asignarse como
                            monitor. Plazas ocupadas: {assignedCount} de {selectedVacancy.plazas}.
                        </div>

                        {slotsFull && (
                            <Alert type="info">
                                Todas las plazas de esta convocatoria ya están ocupadas.
                            </Alert>
                        )}

                        {loadingApplications ? (
                            <div className="text-center mt-16"><Spinner large /></div>
                        ) : applications.length === 0 ? (
                            <p className="muted mt-16">Esta convocatoria aún no tiene postulaciones.</p>
                        ) : (
                            <div className="schedule">
                                {applications.map((p) => (
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
                                            <span className={STATUS_BADGE[p.estado] ?? "badge"}>
                                                {STATUS_LABEL[p.estado] ?? p.estado}
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

                                        {p.estado !== "MONITOR_ASIGNADO" && (
                                        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                                            {p.estado !== "APROBADA" && (
                                                <button
                                                    type="button"
                                                    className="btn btn-ghost btn-sm"
                                                    onClick={() => decide(p, "APROBADA")}
                                                >
                                                    Aprobar
                                                </button>
                                            )}
                                            {p.estado !== "RECHAZADA" && (
                                                <button
                                                    type="button"
                                                    className="btn btn-danger btn-sm"
                                                    onClick={() => decide(p, "RECHAZADA")}
                                                >
                                                    Rechazar
                                                </button>
                                            )}
                                            {p.estado === "APROBADA" && (
                                                <button
                                                    type="button"
                                                    className="btn btn-primary btn-sm"
                                                    onClick={() => promote(p)}
                                                    disabled={slotsFull}
                                                    title={
                                                        slotsFull
                                                            ? "No quedan plazas disponibles en esta convocatoria."
                                                            : undefined
                                                    }
                                                >
                                                    Asignar como monitor
                                                </button>
                                            )}
                                        </div>
                                        )}
                                    </div>
                                ))}
                            </div>
                        )}

                        <div className="modal-actions">
                            <button type="button" className="btn btn-ghost" onClick={() => setSelectedVacancy(null)}>
                                Cerrar
                            </button>
                        </div>
                    </div>
                </div>
                );
            })()}
        </>
    );
}
