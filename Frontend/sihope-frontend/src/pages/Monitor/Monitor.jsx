import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { miDisponibilidad } from "../../services/disponibilidadService";
import { nombreDia, rangoBloque, totalHoras, ordenarBloques } from "../../utils/horario";

/** Panel del monitor. Muestra la disponibilidad real configurada (HU_006). */
export default function Monitor() {
    const { usuario } = useAuth();
    const [bloques, setBloques] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let activo = true;
        miDisponibilidad()
            .then((res) => {
                if (activo) setBloques(ordenarBloques(res.data ?? []));
            })
            .catch((err) => {
                if (activo) setError(err.message);
            })
            .finally(() => {
                if (activo) setCargando(false);
            });
        return () => {
            activo = false;
        };
    }, []);

    const diasConFranjas = new Set(bloques.map((b) => b.diaSemana)).size;

    return (
        <>
            <div className="page-head">
                <h1>Hola, {usuario?.nombre ?? "monitor"} 👋</h1>
                <p>
                    Gestiona tu disponibilidad y revisa las monitorías que tienes asignadas.
                </p>
            </div>

            <Alert tipo="error">{error}</Alert>

            <section className="grid grid--3" style={{ marginBottom: 28 }}>
                <div className="stat">
                    <div className="stat__label">Horas configuradas</div>
                    <div className="stat__value">{cargando ? "…" : totalHoras(bloques)}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Franjas marcadas</div>
                    <div className="stat__value">{cargando ? "…" : bloques.length}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Días con atención</div>
                    <div className="stat__value">{cargando ? "…" : diasConFranjas}</div>
                    <div className="stat__accent" />
                </div>
            </section>

            <section className="grid grid--2">
                <div className="card">
                    <div className="card__title">Mi disponibilidad</div>
                    <div className="card__subtitle">
                        Bloques en los que puedes atender monitorías cada semana.
                    </div>

                    {cargando ? (
                        <div className="text-center mt-16">
                            <Spinner grande />
                        </div>
                    ) : bloques.length === 0 ? (
                        <p className="muted mt-16">
                            Aún no has marcado franjas de atención. Configúralas para que los
                            estudiantes puedan verte.
                        </p>
                    ) : (
                        <div className="schedule">
                            {bloques.map((b, i) => (
                                <div key={`${b.diaSemana}-${b.horaInicio}-${i}`} className="schedule__row">
                                    <span>{nombreDia(b.diaSemana)}</span>
                                    <span className="badge badge-yellow">{rangoBloque(b)}</span>
                                </div>
                            ))}
                        </div>
                    )}

                    <Link to="/monitor/disponibilidad" className="btn btn-primary btn-sm mt-16">
                        Configurar disponibilidad
                    </Link>
                </div>

                <div className="card">
                    <div className="card__title">Próximas monitorías</div>
                    <div className="card__subtitle">Estudiantes que agendaron contigo.</div>
                    <p className="muted mt-16">
                        El agendamiento de citas estará disponible próximamente. Por ahora,
                        mantén tu disponibilidad actualizada.
                    </p>
                </div>
            </section>
        </>
    );
}
