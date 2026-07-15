import { useCallback, useEffect, useMemo, useState } from "react";
import Swal from "sweetalert2";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listMonitors } from "../../services/availabilityService";
import {
    listAsignaturas,
    assignMonitorSubjects
} from "../../services/asignaturaService";

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

export default function CoordinatorMonitors() {
    const [monitors, setMonitors] = useState([]);
    const [catalog, setCatalog] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [selected, setSelected] = useState(null);
    const [checkedIds, setCheckedIds] = useState(new Set());
    const [saving, setSaving] = useState(false);

    const load = useCallback(async () => {
        try {
            const [monitorsRes, catalogRes] = await Promise.all([
                listMonitors(),
                listAsignaturas()
            ]);
            setMonitors(monitorsRes.data ?? []);
            setCatalog(catalogRes.data ?? []);
            setLoadError("");
        } catch (err) {
            setLoadError(err.message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        load();
    }, [load]);

    const idByName = useMemo(() => {
        const map = new Map();
        catalog.forEach((a) => map.set(a.nombre.toLowerCase(), a.id));
        return map;
    }, [catalog]);

    const openAssign = (monitor) => {
        const ids = (monitor.asignaturas ?? [])
            .map((name) => idByName.get(name.toLowerCase()))
            .filter((id) => id != null);
        setCheckedIds(new Set(ids));
        setSelected(monitor);
    };

    const toggle = (id) =>
        setCheckedIds((prev) => {
            const next = new Set(prev);
            if (next.has(id)) next.delete(id);
            else next.add(id);
            return next;
        });

    const save = async () => {
        setSaving(true);
        try {
            const res = await assignMonitorSubjects(selected.id, [...checkedIds]);
            setSelected(null);
            await load();
            toast("success", res.message || "Asignaturas asignadas.");
        } catch (err) {
            const detail = Array.isArray(err.data) ? err.data.join(" ") : "";
            Swal.fire({
                icon: "error",
                title: "No se pudo asignar",
                text: `${err.message} ${detail}`.trim()
            });
        } finally {
            setSaving(false);
        }
    };

    return (
        <>
            <div className="page-head">
                <h1>Asignación de materias</h1>
                <p>
                    Asigna a cada monitor las materias que orientará. Solo puedes elegir
                    asignaturas del catálogo registrado por el administrador; normalmente
                    corresponden a las materias de la convocatoria que ganó.
                </p>
            </div>

            <Alert type="error">{loadError}</Alert>

            <section className="card">
                <div className="card__title">Monitores del programa</div>

                {loading ? (
                    <div className="text-center mt-16">
                        <Spinner large />
                    </div>
                ) : (
                    <div className="table-wrap">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Monitor</th>
                                    <th>Asignaturas que orienta</th>
                                    <th style={{ width: 160 }}>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                {monitors.map((m) => (
                                    <tr key={m.id}>
                                        <td>
                                            <strong>{m.nombre}</strong>
                                            <br />
                                            <span className="muted" style={{ fontSize: "0.82rem" }}>
                                                {m.correo}
                                            </span>
                                        </td>
                                        <td>
                                            {(m.asignaturas ?? []).length === 0 ? (
                                                <span className="muted">Sin materias asignadas</span>
                                            ) : (
                                                <div className="chips">
                                                    {m.asignaturas.map((s) => (
                                                        <span key={s} className="chip">
                                                            {s}
                                                        </span>
                                                    ))}
                                                </div>
                                            )}
                                        </td>
                                        <td>
                                            <button
                                                type="button"
                                                className="btn btn-ghost btn-sm"
                                                onClick={() => openAssign(m)}
                                            >
                                                Asignar materias
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                {monitors.length === 0 && (
                                    <tr>
                                        <td colSpan={3} className="text-center muted">
                                            Aún no hay monitores registrados.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            {selected && (
                <div className="modal-overlay" onClick={() => !saving && setSelected(null)}>
                    <div
                        className="modal-box card"
                        onClick={(e) => e.stopPropagation()}
                        role="dialog"
                        aria-modal="true"
                    >
                        <div className="card__title">Materias de {selected.nombre}</div>
                        <div className="card__subtitle">
                            Marca las asignaturas del catálogo que orientará este monitor.
                        </div>

                        {catalog.length === 0 ? (
                            <p className="muted mt-16">
                                El catálogo está vacío. Pide al administrador que registre las
                                asignaturas.
                            </p>
                        ) : (
                            <div className="schedule mt-16">
                                {catalog.map((a) => (
                                    <label
                                        key={a.id}
                                        className="schedule__row"
                                        style={{ cursor: "pointer", gap: 10 }}
                                    >
                                        <span>{a.nombre}</span>
                                        <input
                                            type="checkbox"
                                            checked={checkedIds.has(a.id)}
                                            onChange={() => toggle(a.id)}
                                        />
                                    </label>
                                ))}
                            </div>
                        )}

                        <div className="modal-actions">
                            <button
                                type="button"
                                className="btn btn-ghost"
                                onClick={() => setSelected(null)}
                                disabled={saving}
                            >
                                Cancelar
                            </button>
                            <button
                                type="button"
                                className="btn btn-primary"
                                onClick={save}
                                disabled={saving || catalog.length === 0}
                            >
                                {saving ? <Spinner /> : "Guardar asignación"}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </>
    );
}
