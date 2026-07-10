import { useEffect, useMemo, useState } from "react";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listarMonitores } from "../../services/disponibilidadService";
import { nombreDia, rangoBloque, ordenarBloques } from "../../utils/horario";

/**
 * Directorio de monitores con datos reales (HU_006, lado de lectura): cada tarjeta
 * muestra el monitor y las franjas horarias que publicó. El filtro por texto se
 * aplica en el cliente sobre nombre y correo.
 */
export default function Monitores() {
    const [monitores, setMonitores] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [error, setError] = useState("");
    const [texto, setTexto] = useState("");

    useEffect(() => {
        let activo = true;
        listarMonitores()
            .then((res) => {
                if (activo) setMonitores(res.data ?? []);
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

    const visibles = useMemo(() => {
        const t = texto.trim().toLowerCase();
        if (!t) return monitores;
        return monitores.filter(
            (m) =>
                (m.nombre ?? "").toLowerCase().includes(t) ||
                (m.correo ?? "").toLowerCase().includes(t)
        );
    }, [texto, monitores]);

    return (
        <>
            <div className="page-head">
                <h1>Monitores disponibles</h1>
                <p>
                    Consulta el perfil de cada monitor y las franjas horarias en las que
                    atiende monitorías.
                </p>
            </div>

            <Alert tipo="error">{error}</Alert>

            <div className="toolbar">
                <input
                    type="search"
                    className="grow"
                    placeholder="Buscar por nombre o correo…"
                    value={texto}
                    onChange={(e) => setTexto(e.target.value)}
                />
            </div>

            {cargando ? (
                <div className="text-center mt-16">
                    <Spinner grande />
                </div>
            ) : visibles.length === 0 ? (
                <Alert tipo="info">
                    {monitores.length === 0
                        ? "Aún no hay monitores registrados en el programa."
                        : "No hay monitores para la búsqueda ingresada."}
                </Alert>
            ) : (
                <section className="grid grid--auto">
                    {visibles.map((m) => {
                        const bloques = ordenarBloques(m.disponibilidad ?? []);
                        return (
                            <article key={m.id} className="card monitor-card">
                                <div className="monitor-card__head">
                                    <span className="monitor-card__av">{m.iniciales}</span>
                                    <div>
                                        <div className="monitor-card__name">{m.nombre}</div>
                                        <div className="monitor-card__prog">{m.correo}</div>
                                    </div>
                                </div>

                                {bloques.length === 0 ? (
                                    <>
                                        <hr className="divider" />
                                        <Alert tipo="info" className="mt-8">
                                            ⓘ Este monitor aún no ha publicado sus horarios de
                                            atención.
                                        </Alert>
                                    </>
                                ) : (
                                    <div>
                                        <strong>Horarios de atención</strong>
                                        <div className="schedule mt-8">
                                            {bloques.map((b, i) => (
                                                <div
                                                    key={`${b.diaSemana}-${b.horaInicio}-${i}`}
                                                    className="schedule__row"
                                                >
                                                    <span>{nombreDia(b.diaSemana)}</span>
                                                    <span className="muted">{rangoBloque(b)}</span>
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
