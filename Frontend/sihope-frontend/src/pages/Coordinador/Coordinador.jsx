import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listarTodas, listarPostulaciones } from "../../services/convocatoriaService";
import { listarMonitores } from "../../services/disponibilidadService";

/**
 * Panel del coordinador. Resume datos reales: convocatorias del periodo (HU_008),
 * postulaciones pendientes (HU_009) y monitores del programa (HU_006).
 */
export default function Coordinador() {
    const { usuario } = useAuth();
    const [convocatorias, setConvocatorias] = useState([]);
    const [monitores, setMonitores] = useState([]);
    const [pendientes, setPendientes] = useState(0);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        let activo = true;

        async function cargar() {
            try {
                const [conv, mon] = await Promise.all([listarTodas(), listarMonitores()]);
                if (!activo) return;
                const lista = conv.data ?? [];
                setConvocatorias(lista);
                setMonitores(mon.data ?? []);

                // Postulaciones pendientes agregadas de las convocatorias abiertas.
                const abiertas = lista.filter((c) => c.estado === "ABIERTA");
                const porConv = await Promise.all(
                    abiertas.map((c) =>
                        listarPostulaciones(c.id)
                            .then((r) => (r.data ?? []).filter((p) => p.estado === "PENDIENTE").length)
                            .catch(() => 0)
                    )
                );
                if (activo) setPendientes(porConv.reduce((a, b) => a + b, 0));
            } catch (err) {
                if (activo) setError(err.message);
            } finally {
                if (activo) setCargando(false);
            }
        }

        cargar();
        return () => {
            activo = false;
        };
    }, []);

    const abiertas = convocatorias.filter((c) => c.estado === "ABIERTA");

    return (
        <>
            <div className="page-head">
                <h1>Hola, {usuario?.nombre ?? "coordinador"} 👋</h1>
                <p>Supervisa las convocatorias, los monitores y la actividad del programa.</p>
            </div>

            <Alert tipo="error">{error}</Alert>

            <section className="grid grid--3" style={{ marginBottom: 28 }}>
                <div className="stat">
                    <div className="stat__label">Monitores activos</div>
                    <div className="stat__value">{cargando ? "…" : monitores.length}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Convocatorias abiertas</div>
                    <div className="stat__value">{cargando ? "…" : abiertas.length}</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Postulaciones por revisar</div>
                    <div className="stat__value">{cargando ? "…" : pendientes}</div>
                    <div className="stat__accent" />
                </div>
            </section>

            <section className="grid grid--2">
                <div className="card">
                    <div className="card__title">Convocatorias en curso</div>
                    <div className="card__subtitle">Procesos de selección de monitores.</div>

                    {cargando ? (
                        <div className="text-center mt-16">
                            <Spinner grande />
                        </div>
                    ) : abiertas.length === 0 ? (
                        <p className="muted mt-16">
                            No tienes convocatorias abiertas. Crea una para iniciar un proceso.
                        </p>
                    ) : (
                        <div className="schedule">
                            {abiertas.slice(0, 5).map((c) => (
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

                    {cargando ? (
                        <div className="text-center mt-16">
                            <Spinner grande />
                        </div>
                    ) : monitores.length === 0 ? (
                        <p className="muted mt-16">Aún no hay monitores registrados.</p>
                    ) : (
                        <div className="schedule">
                            {monitores.slice(0, 5).map((m) => (
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
