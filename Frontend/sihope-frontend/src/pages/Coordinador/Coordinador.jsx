import { Link } from "react-router-dom";
import { useAuth } from "../../hooks/useAuth";

/** Panel del coordinador. Contenido de ejemplo; el saludo usa la sesión. */
export default function Coordinador() {
    const { usuario } = useAuth();

    return (
        <>
            <div className="page-head">
                <h1>Hola, {usuario?.nombre ?? "coordinador"} 👋</h1>
                <p>Supervisa las convocatorias, los monitores y la actividad del programa.</p>
            </div>

            <section className="grid grid--3" style={{ marginBottom: 28 }}>
                <div className="stat">
                    <div className="stat__label">Monitores activos</div>
                    <div className="stat__value">18</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Convocatorias abiertas</div>
                    <div className="stat__value">2</div>
                    <div className="stat__accent" />
                </div>
                <div className="stat">
                    <div className="stat__label">Postulaciones por revisar</div>
                    <div className="stat__value">9</div>
                    <div className="stat__accent" />
                </div>
            </section>

            <section className="grid grid--2">
                <div className="card">
                    <div className="card__title">Convocatorias en curso</div>
                    <div className="card__subtitle">Procesos de selección de monitores.</div>
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
                    <a href="#" className="btn btn-primary btn-sm mt-16">
                        Gestionar convocatorias
                    </a>
                </div>

                <div className="card">
                    <div className="card__title">Monitores del programa</div>
                    <div className="card__subtitle">Revisa perfiles y desempeño.</div>
                    <div className="schedule">
                        <div className="schedule__row">
                            <span>Ana Gómez · Cálculo Diferencial</span>
                            <span className="badge badge-active">Activo</span>
                        </div>
                        <div className="schedule__row">
                            <span>Carlos Ruiz · Programación I</span>
                            <span className="badge badge-active">Activo</span>
                        </div>
                        <div className="schedule__row">
                            <span>Pedro Martínez · Bases de Datos</span>
                            <span className="badge badge-warn">Perfil incompleto</span>
                        </div>
                    </div>
                    <Link to="/monitores" className="btn btn-ghost btn-sm mt-16">
                        Ver todos los monitores
                    </Link>
                </div>
            </section>
        </>
    );
}
