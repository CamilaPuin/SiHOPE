import { useMemo, useState } from "react";
import Alert from "../../components/common/Alert";

/**
 * Directorio de monitores. Datos de ejemplo (aún no hay endpoint); el filtrado
 * por asignatura y texto se hace en el cliente, como en el mockup monitores.html.
 */
const MONITORES = [
    {
        id: 1,
        iniciales: "AG",
        nombre: "Ana Gómez",
        programa: "Ingeniería de Sistemas · 7° semestre",
        asignatura: "Cálculo Diferencial",
        buscar: "ana gómez cálculo límites derivadas",
        temas: ["Límites", "Derivadas", "Continuidad"],
        horarios: [
            { dia: "Lunes", hora: "10:00 – 12:00" },
            { dia: "Miércoles", hora: "14:00 – 16:00" }
        ]
    },
    {
        id: 2,
        iniciales: "CR",
        nombre: "Carlos Ruiz",
        programa: "Ingeniería de Sistemas · 6° semestre",
        asignatura: "Programación I",
        buscar: "carlos ruiz programación java arreglos",
        temas: ["Java", "Arreglos", "Funciones"],
        horarios: [
            { dia: "Martes", hora: "08:00 – 10:00" },
            { dia: "Jueves", hora: "16:00 – 18:00" }
        ]
    },
    {
        id: 3,
        iniciales: "LD",
        nombre: "Laura Díaz",
        programa: "Ingeniería Electrónica · 8° semestre",
        asignatura: "Física Mecánica",
        buscar: "laura díaz física cinemática dinámica",
        temas: ["Cinemática", "Dinámica"],
        horarios: [{ dia: "Viernes", hora: "08:00 – 11:00" }]
    },
    {
        id: 4,
        iniciales: "PM",
        nombre: "Pedro Martínez",
        programa: "Ingeniería de Sistemas · 5° semestre",
        asignatura: "Bases de Datos",
        buscar: "pedro martínez bases de datos",
        temas: [],
        horarios: [],
        incompleto: true
    }
];

const ASIGNATURAS = [
    "Cálculo Diferencial",
    "Programación I",
    "Física Mecánica",
    "Bases de Datos",
    "Estructuras de Datos"
];

export default function Monitores() {
    const [texto, setTexto] = useState("");
    const [asignatura, setAsignatura] = useState("");

    const visibles = useMemo(() => {
        const t = texto.trim().toLowerCase();
        return MONITORES.filter((m) => {
            const coincideAsig = !asignatura || m.asignatura === asignatura;
            const coincideTexto = !t || m.buscar.includes(t);
            return coincideAsig && coincideTexto;
        });
    }, [texto, asignatura]);

    return (
        <>
            <div className="page-head">
                <h1>Monitores disponibles</h1>
                <p>
                    Consulta el perfil de cada monitor: asignaturas, temas y horarios de
                    atención.
                </p>
            </div>

            {/* Filtros */}
            <div className="toolbar">
                <input
                    type="search"
                    className="grow"
                    placeholder="Buscar por monitor o tema…"
                    value={texto}
                    onChange={(e) => setTexto(e.target.value)}
                />
                <select
                    value={asignatura}
                    onChange={(e) => setAsignatura(e.target.value)}
                    aria-label="Filtrar por asignatura"
                >
                    <option value="">Todas las asignaturas</option>
                    {ASIGNATURAS.map((a) => (
                        <option key={a} value={a}>
                            {a}
                        </option>
                    ))}
                </select>
            </div>

            {visibles.length === 0 && (
                <Alert tipo="info">
                    No hay monitores para el filtro seleccionado.
                </Alert>
            )}

            <section className="grid grid--auto">
                {visibles.map((m) => (
                    <article key={m.id} className="card monitor-card">
                        <div className="monitor-card__head">
                            <span className="monitor-card__av">{m.iniciales}</span>
                            <div>
                                <div className="monitor-card__name">{m.nombre}</div>
                                <div className="monitor-card__prog">{m.programa}</div>
                            </div>
                        </div>

                        <div>
                            <strong>Asignaturas</strong>
                            <div className="chips mt-8">
                                <span className="badge badge-role">{m.asignatura}</span>
                            </div>
                        </div>

                        {m.incompleto ? (
                            <>
                                <hr className="divider" />
                                <Alert tipo="info" className="mt-8">
                                    ⓘ Perfil en proceso de actualización. Este monitor aún
                                    no ha publicado sus temas y horarios de atención.
                                </Alert>
                            </>
                        ) : (
                            <>
                                <div>
                                    <strong>Temas</strong>
                                    <div className="chips mt-8">
                                        {m.temas.map((tema) => (
                                            <span key={tema} className="badge badge-yellow">
                                                {tema}
                                            </span>
                                        ))}
                                    </div>
                                </div>
                                <div>
                                    <strong>Horarios de atención</strong>
                                    <div className="schedule mt-8">
                                        {m.horarios.map((h) => (
                                            <div key={h.dia} className="schedule__row">
                                                <span>{h.dia}</span>
                                                <span className="muted">{h.hora}</span>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                                <button type="button" className="btn btn-primary btn-sm">
                                    Agendar monitoría
                                </button>
                            </>
                        )}
                    </article>
                ))}
            </section>
        </>
    );
}
