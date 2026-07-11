import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listAll, listApplications } from "../../services/vacancyService";
import { listMonitors } from "../../services/availabilityService";

export default function Coordinator() {
    const { user } = useAuth();
    const [vacancies, setVacancies] = useState([]);
    const [monitors, setMonitors] = useState([]);
    const [pending, setPending] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;

        async function load() {
            try {
                const [conv, mon] = await Promise.all([listAll(), listMonitors()]);
                if (!active) return;
                const list = conv.data ?? [];
                setVacancies(list);
                setMonitors(mon.data ?? []);

                const open = list.filter((c) => c.estado === "ABIERTA");
                const perVacancy = await Promise.all(
                    open.map((c) =>
                        listApplications(c.id)
                            .then((r) => (r.data ?? []).filter((p) => p.estado === "PENDIENTE").length)
                            .catch(() => 0)
                    )
                );
                if (active) setPending(perVacancy.reduce((a, b) => a + b, 0));
            } catch (err) {
                if (active) setError(err.message);
            } finally {
                if (active) setLoading(false);
            }
        }

        load();
        return () => {
            active = false;
        };
    }, []);

    const open = vacancies.filter((c) => c.estado === "ABIERTA");

    return (
        <>
            <div className="page-head">
                <h1>Hola, {user?.nombre ?? "coordinador"} </h1>
                <p>Supervisa las convocatorias, los monitores y la actividad del programa.</p>
            </div>

            <Alert type="error">{error}</Alert>

            <section className="grid grid--3" style={{ marginBottom: 28 }}>
                <div className="stat">
                    <div className="stat__label">Monitores activos</div>
                    <div className="stat__value">{loading ? "…" : monitors.length}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Convocatorias abiertas</div>
                    <div className="stat__value">{loading ? "…" : open.length}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Postulaciones por revisar</div>
                    <div className="stat__value">{loading ? "…" : pending}</div>
                    <div className="stat__accent" />
                </div>
            </section>

            <section className="grid grid--2">
                <div className="card">
                    <div className="card__title">Convocatorias en curso</div>
                    <div className="card__subtitle">Procesos de selección de monitores.</div>

                    {loading ? (
                        <div className="text-center mt-16">
                            <Spinner large />
                        </div>
                    ) : open.length === 0 ? (
                        <p className="muted mt-16">
                            No tienes convocatorias abiertas. Crea una para iniciar un proceso.
                        </p>
                    ) : (
                        <div className="schedule">
                            {open.slice(0, 5).map((c) => (
                                <div key={c.id} className="schedule__row">
                                    <span>
                                        <strong>{c.titulo}</strong> · {c.materia}
                                    </span>
                                    <span className="badge badge-yellow">
                                        Cierra {c.fechaLimite}
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}

                    <Link to="/coordinador/convocatorias" className="btn btn-primary btn-sm mt-16">
                        Gestionar convocatorias
                    </Link>
                </div>

                <div className="card">
                    <div className="card__title">Monitores del programa</div>
                    <div className="card__subtitle">Monitores registrados y su disponibilidad.</div>

                    {loading ? (
                        <div className="text-center mt-16">
                            <Spinner large />
                        </div>
                    ) : monitors.length === 0 ? (
                        <p className="muted mt-16">Aún no hay monitores registrados.</p>
                    ) : (
                        <div className="schedule">
                            {monitors.slice(0, 5).map((m) => (
                                <div key={m.id} className="schedule__row">
                                    <span>{m.nombre}</span>
                                    {(m.disponibilidad ?? []).length > 0 ? (
                                        <span className="badge badge-active">
                                            {m.disponibilidad.length} franja(s)
                                        </span>
                                    ) : (
                                        <span className="badge badge-warn">Sin horario</span>
                                    )}
                                </div>
                            ))}
                        </div>
                    )}

                    <Link to="/monitores" className="btn btn-ghost btn-sm mt-16">
                        Ver todos los monitores
                    </Link>
                </div>
            </section>
        </>
    );
}
