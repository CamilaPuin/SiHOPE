import { useEffect, useState } from "react";
import FullCalendar from "@fullcalendar/react";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import Swal from "sweetalert2";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import {
    myAvailability,
    saveAvailability
} from "../../services/availabilityService";

let counter = 0;
const newId = () => `bloque-${counter++}`;

const MAX_TOTAL_MINUTES = 8 * 60;

function mondayOfThisWeek() {
    const today = new Date();
    const day = today.getDay();
    const diff = day === 0 ? -6 : 1 - day;
    const monday = new Date(today);
    monday.setDate(today.getDate() + diff);
    monday.setHours(0, 0, 0, 0);
    return monday;
}

const twoDigits = (n) => String(n).padStart(2, "0");
const toHHmm = (date) => `${twoDigits(date.getHours())}:${twoDigits(date.getMinutes())}`;

const weekdayOf = (date) => (date.getDay() === 0 ? 7 : date.getDay());

function blockDate(monday, diaSemana, hhmm) {
    const [h, m] = hhmm.split(":").map(Number);
    const f = new Date(monday);
    f.setDate(monday.getDate() + (diaSemana - 1));
    f.setHours(h, m, 0, 0);
    return f;
}

export default function MonitorAvailability() {
    const [monday] = useState(mondayOfThisWeek);
    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [loadError, setLoadError] = useState("");

    useEffect(() => {
        let active = true;
        myAvailability()
            .then((res) => {
                if (!active) return;
                const loaded = (res.data ?? []).map((b) => ({
                    id: newId(),
                    start: blockDate(monday, b.diaSemana, b.horaInicio),
                    end: blockDate(monday, b.diaSemana, b.horaFin)
                }));
                setEvents(loaded);
            })
            .catch((err) => {
                if (active) setLoadError(err.message);
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, [monday]);

    const handleSelect = (info) => {
        setEvents((prev) => [
            ...prev,
            { id: newId(), start: info.start, end: info.end }
        ]);
        info.view.calendar.unselect();
    };

    const handleEventClick = (info) => {
        setEvents((prev) => prev.filter((e) => e.id !== info.event.id));
    };

    const save = async () => {
        const bloques = events.map((e) => {
            const start = new Date(e.start);
            const end = new Date(e.end);
            return {
                diaSemana: weekdayOf(start),
                horaInicio: toHHmm(start),
                horaFin: toHHmm(end)
            };
        });

        const totalMinutes = bloques.reduce((acc, b) => {
            const [hi, mi] = b.horaInicio.split(":").map(Number);
            const [hf, mf] = b.horaFin.split(":").map(Number);
            return acc + Math.max(0, hf * 60 + mf - (hi * 60 + mi));
        }, 0);
        if (totalMinutes > MAX_TOTAL_MINUTES) {
            Swal.fire({
                icon: "warning",
                title: "Superas el máximo de 8 horas",
                text: `Tu disponibilidad suma ${(totalMinutes / 60).toFixed(1)} h. `
                    + "Elimina algunas franjas para no exceder las 8 horas en total."
            });
            return;
        }

        setSaving(true);
        try {
            const res = await saveAvailability(bloques);
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
            const detail = Array.isArray(err.data) ? err.data.join(" ") : "";
            Swal.fire({
                icon: "error",
                title: "No se pudo guardar",
                text: `${err.message} ${detail}`.trim()
            });
        } finally {
            setSaving(false);
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

            <Alert type="error">{loadError}</Alert>

            <section className="card">
                {loading ? (
                    <div className="text-center mt-16">
                        <Spinner large />
                    </div>
                ) : (
                    <>
                        <FullCalendar
                            plugins={[timeGridPlugin, interactionPlugin]}
                            initialView="timeGridWeek"
                            initialDate={monday}
                            firstDay={1}
                            hiddenDays={[0]}
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
                            select={handleSelect}
                            eventClick={handleEventClick}
                            events={events}
                            eventColor="#e0a80d"
                            eventTextColor="#1a1a1a"
                        />
                        <div className="mt-16">
                            <button
                                type="button"
                                className="btn btn-primary"
                                onClick={save}
                                disabled={saving}
                            >
                                {saving ? <Spinner /> : "Guardar disponibilidad"}
                            </button>
                        </div>
                    </>
                )}
            </section>
        </>
    );
}
