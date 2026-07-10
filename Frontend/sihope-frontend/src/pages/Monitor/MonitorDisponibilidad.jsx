import { useEffect, useRef, useState } from "react";
import FullCalendar from "@fullcalendar/react";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import Swal from "sweetalert2";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import {
    miDisponibilidad,
    guardarDisponibilidad
} from "../../services/disponibilidadService";

/**
 * Configuración de la disponibilidad horaria del monitor (HU_006).
 *
 * Se usa FullCalendar (librería de calendario probada, según la mitigación del
 * sprint) en vista semanal: el monitor arrastra para marcar franjas y hace clic
 * para eliminarlas. Como la disponibilidad es semanal recurrente, todas las
 * franjas se dibujan sobre una semana de referencia (la semana actual) y al
 * guardar se traducen a { diaSemana 1-7, horaInicio, horaFin }.
 */

let contador = 0;
const nuevoId = () => `bloque-${contador++}`;

/** Lunes 00:00 de la semana actual: ancla para dibujar las franjas recurrentes. */
function lunesDeEstaSemana() {
    const hoy = new Date();
    const dia = hoy.getDay(); // 0=Domingo … 6=Sábado
    const diff = dia === 0 ? -6 : 1 - dia;
    const lunes = new Date(hoy);
    lunes.setDate(hoy.getDate() + diff);
    lunes.setHours(0, 0, 0, 0);
    return lunes;
}

const dosDigitos = (n) => String(n).padStart(2, "0");
const aHHmm = (fecha) => `${dosDigitos(fecha.getHours())}:${dosDigitos(fecha.getMinutes())}`;

/** diaSemana 1-7 (Lunes-Domingo) a partir de un Date. */
const diaSemanaDe = (fecha) => (fecha.getDay() === 0 ? 7 : fecha.getDay());

/** Construye el Date de una franja recurrente sobre la semana de referencia. */
function fechaDeBloque(lunes, diaSemana, hhmm) {
    const [h, m] = hhmm.split(":").map(Number);
    const f = new Date(lunes);
    f.setDate(lunes.getDate() + (diaSemana - 1));
    f.setHours(h, m, 0, 0);
    return f;
}

export default function MonitorDisponibilidad() {
    const lunesRef = useRef(lunesDeEstaSemana());
    const [eventos, setEventos] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [guardando, setGuardando] = useState(false);
    const [errorCarga, setErrorCarga] = useState("");

    useEffect(() => {
        let activo = true;
        miDisponibilidad()
            .then((res) => {
                if (!activo) return;
                const lunes = lunesRef.current;
                const cargados = (res.data ?? []).map((b) => ({
                    id: nuevoId(),
                    start: fechaDeBloque(lunes, b.diaSemana, b.horaInicio),
                    end: fechaDeBloque(lunes, b.diaSemana, b.horaFin)
                }));
                setEventos(cargados);
            })
            .catch((err) => {
                if (activo) setErrorCarga(err.message);
            })
            .finally(() => {
                if (activo) setCargando(false);
            });
        return () => {
            activo = false;
        };
    }, []);

    const manejarSeleccion = (info) => {
        setEventos((prev) => [
            ...prev,
            { id: nuevoId(), start: info.start, end: info.end }
        ]);
        info.view.calendar.unselect();
    };

    const manejarClicEvento = (info) => {
        setEventos((prev) => prev.filter((e) => e.id !== info.event.id));
    };

    const guardar = async () => {
        // Traducir eventos de la semana de referencia a bloques recurrentes.
        const bloques = eventos.map((e) => {
            const inicio = new Date(e.start);
            const fin = new Date(e.end);
            return {
                diaSemana: diaSemanaDe(inicio),
                horaInicio: aHHmm(inicio),
                horaFin: aHHmm(fin)
            };
        });

        setGuardando(true);
        try {
            const res = await guardarDisponibilidad(bloques);
            Swal.fire({
                toast: true,
                position: "top-end",
                showConfirmButton: false,
                timer: 2600,
                timerProgressBar: true,
                icon: "success",
                title: res.message || "Disponibilidad guardada."
            });
        } catch (err) {
            // err.data puede traer la lista de errores de validación del backend.
            const detalle = Array.isArray(err.data) ? err.data.join(" ") : "";
            Swal.fire({
                icon: "error",
                title: "No se pudo guardar",
                text: `${err.message} ${detalle}`.trim()
            });
        } finally {
            setGuardando(false);
        }
    };

    return (
        <>
            <div className="page-head">
                <h1>Mi disponibilidad</h1>
                <p>
                    Arrastra sobre el calendario para marcar las franjas en las que puedes
                    atender monitorías. Haz clic en una franja para eliminarla. Solo los
                    horarios marcados quedarán visibles para los estudiantes.
                </p>
            </div>

            <Alert tipo="error">{errorCarga}</Alert>

            <section className="card">
                {cargando ? (
                    <div className="text-center mt-16">
                        <Spinner grande />
                    </div>
                ) : (
                    <>
                        <FullCalendar
                            plugins={[timeGridPlugin, interactionPlugin]}
                            initialView="timeGridWeek"
                            initialDate={lunesRef.current}
                            firstDay={1}
                            locale="es"
                            allDaySlot={false}
                            slotMinTime="06:00:00"
                            slotMaxTime="22:00:00"
                            headerToolbar={false}
                            dayHeaderFormat={{ weekday: "long" }}
                            height="auto"
                            selectable
                            selectMirror
                            selectOverlap={false}
                            eventOverlap={false}
                            select={manejarSeleccion}
                            eventClick={manejarClicEvento}
                            events={eventos}
                            eventColor="#e0a80d"
                            eventTextColor="#1a1a1a"
                        />
                        <div className="mt-16">
                            <button
                                type="button"
                                className="btn btn-primary"
                                onClick={guardar}
                                disabled={guardando}
                            >
                                {guardando ? <Spinner /> : "Guardar disponibilidad"}
                            </button>
                        </div>
                    </>
                )}
            </section>
        </>
    );
}
