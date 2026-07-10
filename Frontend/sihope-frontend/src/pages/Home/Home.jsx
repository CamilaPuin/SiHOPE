import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listarAbiertas } from "../../services/convocatoriaService";
import { listarMonitores } from "../../services/disponibilidadService";

/**
 * Panel principal del estudiante. Resume datos reales: convocatorias abiertas
 * (HU_005) y monitores disponibles (HU_006, lado de lectura).
 */
export default function Home() {
    const { usuario } = useAuth();
    const [convocatorias, setConvocatorias] = useState([]);
    const [monitores, setMonitores] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let activo = true;
        Promise.all([listarAbiertas(), listarMonitores()])
            .then(([conv, mon]) => {
                if (!activo) return;
                setConvocatorias(conv.data ?? []);
                setMonitores(mon.data ?? []);
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

    const monitoresConHorario = monitores.filter((m) => (m.disponibilidad ?? []).length > 0).length;

    return (
        <>
            <div className="page-head">
                <h1>Hola, {usuario?.nombre ?? "estudiante"} 👋</h1>
                <p>
                    Este es el resumen de tu actividad en el programa de monitorías
                    académicas.
                </p>
            </div>

            <Alert tipo="error">{error}</Alert>

            <section className="grid grid--3" style={{ marginBottom: 28 }}>
                <div className="stat">
                    <div className="stat__label">Convocatorias abiertas</div>
                    <div className="stat__value">{cargando ? "…" : convocatorias.length}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Monitores disponibles</div>
                    <div className="stat__value">{cargando ? "…" : monitoresConHorario}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Monitores registrados</div>
                    <div className="stat__value">{cargando ? "…" : monitores.length}</div>
                    <div className="stat__accent" />
                </div>
            </section>

            <section className="grid grid--2">
                <div className="card">
                    <div className="card__title">Convocatorias abiertas</div>
                    <div className="card__subtitle">
                        Postúlate para ser monitor este semestre.
                    </div>

                    {cargando ? (
                        <div className="text-center mt-16">
                            <Spinner grande />
                        </div>
                    ) : convocatorias.length === 0 ? (
                        <p className="muted mt-16">
                            No hay convocatorias abiertas en este momento.
                        </p>
                    ) : (
                        <div className="schedule">
                            {convocatorias.slice(0, 4).map((c) => (
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

                    {cargando ? (
                        <div className="text-center mt-16">
                            <Spinner grande />
                        </div>
                    ) : monitoresConHorario === 0 ? (
                        <p className="muted mt-16">
                            Todavía ningún monitor ha publicado horarios disponibles.
                        </p>
                    ) : (
                        <div className="schedule">
                            {monitores
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
