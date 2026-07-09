import { useAuth } from "../../hooks/useAuth";

/** Panel del monitor. Contenido de ejemplo; el saludo usa la sesión. */
export default function Monitor() {
    const { usuario } = useAuth();

    return (
        <>
            <div className="page-head">
                <h1>Hola, {usuario?.nombre ?? "monitor"} 👋</h1>
                <p>
                    Gestiona tu disponibilidad y revisa las monitorías que tienes asignadas.
                </p>
            </div>

            <section className="grid grid--3" style={{ marginBottom: 28 }}>
                <div className="stat">
                    <div className="stat__label">Horas configuradas</div>
                    <div className="stat__value">8 h</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Próximas citas</div>
                    <div className="stat__value">3</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Estudiantes atendidos</div>
                    <div className="stat__value">27</div>
                    <div className="stat__accent" />
                </div>
            </section>

            <section className="grid grid--2">
                <div className="card">
                    <div className="card__title">Mi disponibilidad</div>
                    <div className="card__subtitle">
                        Bloques en los que puedes atender monitorías esta semana.
                    </div>
                    <div className="schedule">
                        <div className="schedule__row">
                            <span>Lunes</span>
                            <span className="badge badge-yellow">10:00 – 12:00</span>
                        </div>
                        <div className="schedule__row">
                            <span>Miércoles</span>
                            <span className="badge badge-yellow">14:00 – 16:00</span>
                        </div>
                        <div className="schedule__row">
                            <span>Viernes</span>
                            <span className="muted">Sin definir</span>
                        </div>
                    </div>
                    <a href="#" className="btn btn-primary btn-sm mt-16">
                        Configurar disponibilidad
                    </a>
                </div>

                <div className="card">
                    <div className="card__title">Próximas monitorías</div>
                    <div className="card__subtitle">Estudiantes que agendaron contigo.</div>
                    <div className="schedule">
                        <div className="schedule__row">
                            <span>
                                <strong>Cálculo Diferencial</strong> · Sara P.
                            </span>
                            <span className="muted">Lun 10:00</span>
                        </div>
                        <div className="schedule__row">
                            <span>
                                <strong>Cálculo Diferencial</strong> · Andrés M.
                            </span>
                            <span className="muted">Mié 14:00</span>
                        </div>
                    </div>
                </div>
            </section>
        </>
    );
}
