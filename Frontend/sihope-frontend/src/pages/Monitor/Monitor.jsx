import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { myAvailability } from "../../services/availabilityService";
import { mySubjects } from "../../services/asignaturaService";
import { dayName, blockRange, totalHours, sortBlocks } from "../../utils/schedule";

export default function Monitor() {
    const { user } = useAuth();
    const [blocks, setBlocks] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        Promise.all([myAvailability(), mySubjects()])
            .then(([availabilityRes, subjectsRes]) => {
                if (!active) return;
                setBlocks(sortBlocks(availabilityRes.data ?? []));
                setSubjects(subjectsRes.data ?? []);
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

    const daysWithSlots = new Set(blocks.map((b) => b.diaSemana)).size;

    return (
        <>
            <div className="page-head">
                <h1>Hola, {user?.nombre ?? "monitor"} </h1>
                <p>
                    Gestiona tu disponibilidad y revisa las monitorías que tienes asignadas.
                </p>
            </div>

            <Alert type="error">{error}</Alert>

            <section className="grid grid--3" style={{ marginBottom: 28 }}>
                <div className="stat">
                    <div className="stat__label">Horas configuradas</div>
                    <div className="stat__value">{loading ? "…" : totalHours(blocks)}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Franjas marcadas</div>
                    <div className="stat__value">{loading ? "…" : blocks.length}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Días con atención</div>
                    <div className="stat__value">{loading ? "…" : daysWithSlots}</div>
                    <div className="stat__accent" />
                </div>
            </section>

            <section className="grid grid--2">
                <div className="card">
                    <div className="card__title">Mi disponibilidad</div>
                    <div className="card__subtitle">
                        Bloques en los que puedes atender monitorías cada semana.
                    </div>

                    {loading ? (
                        <div className="text-center mt-16">
                            <Spinner large />
                        </div>
                    ) : blocks.length === 0 ? (
                        <p className="muted mt-16">
                            Aún no has marcado franjas de atención. Configúralas para que los
                            estudiantes puedan verte.
                        </p>
                    ) : (
                        <div className="schedule">
                            {blocks.map((b, i) => (
                                <div key={`${b.diaSemana}-${b.horaInicio}-${i}`} className="schedule__row">
                                    <span>{dayName(b.diaSemana)}</span>
                                    <span className="badge badge-yellow">{blockRange(b)}</span>
                                </div>
                            ))}
                        </div>
                    )}

                    <Link to="/monitor/disponibilidad" className="btn btn-primary btn-sm mt-16">
                        Configurar disponibilidad
                    </Link>
                </div>

                <div className="card">
                    <div className="card__title">Mis asignaturas</div>
                    <div className="card__subtitle">
                        Materias que orientas, asignadas por el coordinador.
                    </div>

                    {loading ? (
                        <div className="text-center mt-16">
                            <Spinner large />
                        </div>
                    ) : subjects.length === 0 ? (
                        <p className="muted mt-16">
                            Aún no tienes materias asignadas. El coordinador te las asignará
                            según la convocatoria que ganaste.
                        </p>
                    ) : (
                        <div className="chips mt-16">
                            {subjects.map((s) => (
                                <span key={s} className="chip">
                                    {s}
                                </span>
                            ))}
                        </div>
                    )}
                </div>
            </section>
        </>
    );
}
