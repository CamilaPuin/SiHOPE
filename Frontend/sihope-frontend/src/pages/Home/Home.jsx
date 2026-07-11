import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listOpen } from "../../services/vacancyService";
import { listMonitors } from "../../services/availabilityService";

export default function Home() {
    const { user } = useAuth();
    const [vacancies, setVacancies] = useState([]);
    const [monitors, setMonitors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        Promise.all([listOpen(), listMonitors()])
            .then(([conv, mon]) => {
                if (!active) return;
                setVacancies(conv.data ?? []);
                setMonitors(mon.data ?? []);
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
    }, []);

    const monitorsWithSchedule = monitors.filter((m) => (m.disponibilidad ?? []).length > 0).length;

    return (
        <>
            <div className="page-head">
                <h1>Hola, {user?.nombre ?? "estudiante"} </h1>
                <p>
                    Este es el resumen de tu actividad en el programa de monitorías
                    académicas.
                </p>
            </div>

            <Alert type="error">{error}</Alert>

            <section className="grid grid--3" style={{ marginBottom: 28 }}>
                <div className="stat">
                    <div className="stat__label">Convocatorias abiertas</div>
                    <div className="stat__value">{loading ? "…" : vacancies.length}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Monitores disponibles</div>
                    <div className="stat__value">{loading ? "…" : monitorsWithSchedule}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Monitores registrados</div>
                    <div className="stat__value">{loading ? "…" : monitors.length}</div>
                    <div className="stat__accent" />
                </div>
            </section>

            <section className="grid grid--2">
                <div className="card">
                    <div className="card__title">Convocatorias abiertas</div>
                    <div className="card__subtitle">
                        Postúlate para ser monitor este semestre.
                    </div>

                    {loading ? (
                        <div className="text-center mt-16">
                            <Spinner large />
                        </div>
                    ) : vacancies.length === 0 ? (
                        <p className="muted mt-16">
                            No hay convocatorias abiertas en este momento.
                        </p>
                    ) : (
                        <div className="schedule">
                            {vacancies.slice(0, 4).map((c) => (
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

                    <Link to="/convocatorias" className="btn btn-ghost btn-sm mt-16">
                        Ver convocatorias
                    </Link>
                </div>

                <div className="card">
                    <div className="card__title">Monitores con disponibilidad</div>
                    <div className="card__subtitle">
                        Monitores que ya publicaron sus horarios de atención.
                    </div>

                    {loading ? (
                        <div className="text-center mt-16">
                            <Spinner large />
                        </div>
                    ) : monitorsWithSchedule === 0 ? (
                        <p className="muted mt-16">
                            Todavía ningún monitor ha publicado horarios disponibles.
                        </p>
                    ) : (
                        <div className="schedule">
                            {monitors
                                .filter((m) => (m.disponibilidad ?? []).length > 0)
                                .slice(0, 4)
                                .map((m) => (
                                    <div key={m.id} className="schedule__row">
                                        <span>
                                            <strong>{m.nombre}</strong>
                                        </span>
                                        <span className="badge badge-active">
                                            {m.disponibilidad.length} franja(s)
                                        </span>
                                    </div>
                                ))}
                        </div>
                    )}

                    <Link to="/monitores" className="btn btn-ghost btn-sm mt-16">
                        Ver monitores
                    </Link>
                </div>
            </section>
        </>
    );
}
