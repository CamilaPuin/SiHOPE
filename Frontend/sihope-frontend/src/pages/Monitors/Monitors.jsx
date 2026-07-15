import { useEffect, useMemo, useState } from "react";
import Swal from "sweetalert2";
import { useNavigate } from "react-router-dom";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useAuth } from "../../hooks/useAuth";
import { listMonitors } from "../../services/availabilityService";
import { listAsignaturas } from "../../services/asignaturaService";
import { dayName, blockRange, sortBlocks } from "../../utils/schedule";

export default function Monitors() {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [monitors, setMonitors] = useState([]);
    const [catalog, setCatalog] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [query, setQuery] = useState("");
    const [asignaturaId, setAsignaturaId] = useState("");

    const isStudent = user?.rol === "ESTUDIANTE";

    useEffect(() => {
        listAsignaturas()
            .then((res) => setCatalog(res.data ?? []))
            .catch(() => setCatalog([]));
    }, []);

    useEffect(() => {
        let active = true;
        setLoading(true);
        setError("");
        listMonitors(asignaturaId || undefined)
            .then((res) => {
                if (active) setMonitors(res.data ?? []);
            })
            .catch((err) => {
                if (active) setError(err.message);
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, [asignaturaId]);

    const visible = useMemo(() => {
        const t = query.trim().toLowerCase();
        if (!t) return monitors;
        return monitors.filter(
            (m) =>
                (m.nombre ?? "").toLowerCase().includes(t) ||
                (m.correo ?? "").toLowerCase().includes(t)
        );
    }, [query, monitors]);

    return (
        <>
            <div className="page-head">
                <h1>Monitores disponibles</h1>
                <p>
                    Consulta el perfil de cada monitor, las asignaturas que atiende y las
                    franjas horarias en las que ofrece monitorías.
                </p>
            </div>

            <Alert type="error">{error}</Alert>

            <div className="toolbar">
                <input
                    type="search"
                    className="grow"
                    placeholder="Buscar por nombre o correo…"
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                />
                <select
                    value={asignaturaId}
                    onChange={(e) => setAsignaturaId(e.target.value)}
                >
                    <option value="">Todas las asignaturas</option>
                    {catalog.map((a) => (
                        <option key={a.id} value={a.id}>
                            {a.nombre}
                        </option>
                    ))}
                </select>
            </div>

            {loading ? (
                <div className="text-center mt-16">
                    <Spinner large />
                </div>
            ) : visible.length === 0 ? (
                <Alert type="info">
                    {monitors.length === 0
                        ? asignaturaId
                            ? "Ningún monitor atiende la asignatura seleccionada."
                            : "Aún no hay monitores registrados en el programa."
                        : "No hay monitores para la búsqueda ingresada."}
                </Alert>
            ) : (
                <section className="grid grid--auto">
                    {visible.map((m) => {
                        const blocks = sortBlocks(m.disponibilidad ?? []);
                        const subjects = m.asignaturas ?? [];
                        return (
                            <article key={m.id} className="card monitor-card">
                                <div className="monitor-card__head">
                                    <span className="monitor-card__av">{m.iniciales}</span>
                                    <div>
                                        <div className="monitor-card__name">{m.nombre}</div>
                                        <div className="monitor-card__prog">{m.correo}</div>
                                    </div>
                                </div>

                                <div className="mt-8">
                                    <strong>Asignaturas</strong>
                                    {subjects.length === 0 ? (
                                        <p className="muted mt-8">
                                            Este monitor aún no ha registrado asignaturas.
                                        </p>
                                    ) : (
                                        <div className="chips mt-8">
                                            {subjects.map((s) => (
                                                <span key={s} className="chip chip--muted">
                                                    {s}
                                                </span>
                                            ))}
                                        </div>
                                    )}
                                </div>

                                {blocks.length === 0 ? (
                                    <>
                                        <hr className="divider" />
                                        <Alert type="info" className="mt-8">
                                            ⓘ Este monitor aún no ha publicado sus horarios de
                                            atención.
                                        </Alert>
                                    </>
                                ) : (
                                    <div className="mt-16">
                                        <strong>Horarios de atención</strong>
                                        <div className="schedule mt-8">
                                            {blocks.map((b, i) => (
                                                <div
                                                    key={`${b.diaSemana}-${b.horaInicio}-${i}`}
                                                    className="schedule__row"
                                                >
                                                    <span>{dayName(b.diaSemana)}</span>
                                                    <span className="muted">{blockRange(b)}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}

                                {isStudent && subjects.length > 0 && (
                                    <button
                                        type="button"
                                        className="btn btn-primary btn-sm mt-16"
                                        onClick={() => {
                                            if (blocks.length > 0) {
                                                navigate(`/agendar/${m.id}`);
                                            } else {
                                                Swal.fire({
                                                    icon: "info",
                                                    title: "Sin horarios disponibles",
                                                    text: "Este monitor aún no tiene franjas de atención publicadas. Intenta con otro monitor o vuelve más tarde.",
                                                });
                                            }
                                        }}
                                        disabled={blocks.length === 0}
                                    >
                                        Agendar cita
                                    </button>
                                )}
                            </article>
                        );
                    })}
                </section>
            )}
        </>
    );
}
