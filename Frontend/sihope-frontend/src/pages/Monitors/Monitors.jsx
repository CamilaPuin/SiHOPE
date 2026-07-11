import { useEffect, useMemo, useState } from "react";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listMonitors } from "../../services/availabilityService";
import { dayName, blockRange, sortBlocks } from "../../utils/schedule";

export default function Monitors() {
    const [monitors, setMonitors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [query, setQuery] = useState("");

    useEffect(() => {
        let active = true;
        listMonitors()
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
    }, []);

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
                    Consulta el perfil de cada monitor y las franjas horarias en las que
                    atiende monitorías.
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
            </div>

            {loading ? (
                <div className="text-center mt-16">
                    <Spinner large />
                </div>
            ) : visible.length === 0 ? (
                <Alert type="info">
                    {monitors.length === 0
                        ? "Aún no hay monitores registrados en el programa."
                        : "No hay monitores para la búsqueda ingresada."}
                </Alert>
            ) : (
                <section className="grid grid--auto">
                    {visible.map((m) => {
                        const blocks = sortBlocks(m.disponibilidad ?? []);
                        return (
                            <article key={m.id} className="card monitor-card">
                                <div className="monitor-card__head">
                                    <span className="monitor-card__av">{m.iniciales}</span>
                                    <div>
                                        <div className="monitor-card__name">{m.nombre}</div>
                                        <div className="monitor-card__prog">{m.correo}</div>
                                    </div>
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
                                    <div>
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
                            </article>
                        );
                    })}
                </section>
            )}
        </>
    );
}
