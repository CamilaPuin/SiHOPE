import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

/**
 * Panel principal del estudiante. Contenido de ejemplo (aún no hay endpoints
 * para citas/convocatorias); el saludo usa el usuario en sesión.
 */
export default function Home() {
    const { usuario } = useAuth();

    return (
        <>
            <div className="page-head">
                <h1>Hola, {usuario?.nombre ?? "estudiante"} 👋</h1>
                <p>
                    Este es el resumen de tu actividad en el programa de monitorías
                    académicas.
                </p>
            </div>

            <section className="grid grid--3" style={{ marginBottom: 28 }}>
                <div className="stat">
                    <div className="stat__label">Próximas citas</div>
                    <div className="stat__value">3</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Monitores disponibles</div>
                    <div className="stat__value">18</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Convocatorias abiertas</div>
                    <div className="stat__value">2</div>
                    <div className="stat__accent" />
                </div>
            </section>

            <section className="grid grid--2">
                <div className="card">
                    <div className="card__title">Próximas citas</div>
                    <div className="card__subtitle">
                        Tus monitorías agendadas para esta semana.
                    </div>
                    <div className="schedule">
                        <div className="schedule__row">
                            <span>
                                <strong>Cálculo Diferencial</strong> · Ana Gómez
                            </span>
                            <span className="muted">Lun 10:00</span>
                        </div>
                        <div className="schedule__row">
                            <span>
                                <strong>Programación I</strong> · Carlos Ruiz
                            </span>
                            <span className="muted">Mié 14:00</span>
                        </div>
                        <div className="schedule__row">
                            <span>
                                <strong>Física Mecánica</strong> · Laura Díaz
                            </span>
                            <span className="muted">Vie 08:00</span>
                        </div>
                    </div>
                </div>

                <div className="card">
                    <div className="card__title">Convocatorias abiertas</div>
                    <div className="card__subtitle">
                        Postúlate para ser monitor este semestre.
                    </div>
                    <div className="schedule">
                        <div className="schedule__row">
                            <span>
                                <strong>Monitor de Estructuras de Datos</strong>
                            </span>
                            <span className="badge badge-yellow">Cierra 20 jul</span>
                        </div>
                        <div className="schedule__row">
                            <span>
                                <strong>Monitor de Bases de Datos</strong>
                            </span>
                            <span className="badge badge-yellow">Cierra 28 jul</span>
                        </div>
                    </div>
                    <Link to="/monitores" className="btn btn-ghost btn-sm mt-16">
                        Ver más
                    </Link>
                </div>
            </section>
        </>
    );
}
