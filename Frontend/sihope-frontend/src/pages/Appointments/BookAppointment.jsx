import { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Swal from "sweetalert2";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listMonitors } from "../../services/availabilityService";
import { freeSlots, bookCita } from "../../services/citaService";
import { hhmm } from "../../utils/schedule";

const todayISO = () => {
    const d = new Date();
    const off = d.getTimezoneOffset();
    return new Date(d.getTime() - off * 60000).toISOString().slice(0, 10);
};

export default function BookAppointment() {
    const { monitorId } = useParams();
    const navigate = useNavigate();

    const [monitor, setMonitor] = useState(null);
    const [asignatura, setAsignatura] = useState("");
    const [date, setDate] = useState("");
    const [slots, setSlots] = useState([]);
    const [selectedSlot, setSelectedSlot] = useState("");
    const [loading, setLoading] = useState(true);
    const [loadingSlots, setLoadingSlots] = useState(false);
    const [booking, setBooking] = useState(false);
    const [error, setError] = useState("");

    // catálogo de asignaturas del monitor -> se necesita el id; lo tomamos del catálogo global
    const [catalog, setCatalog] = useState([]);

    useEffect(() => {
        let active = true;
        Promise.all([
            listMonitors(),
            import("../../services/asignaturaService").then((m) => m.listAsignaturas())
        ])
            .then(([mon, cat]) => {
                if (!active) return;
                const found = (mon.data ?? []).find((m) => String(m.id) === String(monitorId));
                setMonitor(found ?? null);
                setCatalog(cat.data ?? []);
                if (!found) setError("El monitor solicitado no existe.");
            })
            .catch((err) => active && setError(err.message))
            .finally(() => active && setLoading(false));
        return () => {
            active = false;
        };
    }, [monitorId]);

    // asignaturas del monitor emparejadas con su id del catálogo
    const monitorSubjects = useMemo(() => {
        const names = monitor?.asignaturas ?? [];
        return names
            .map((name) => catalog.find((c) => c.nombre === name))
            .filter(Boolean);
    }, [monitor, catalog]);

    const loadSlots = (d) => {
        setSelectedSlot("");
        setSlots([]);
        if (!d) return;
        setLoadingSlots(true);
        freeSlots(monitorId, d)
            .then((res) => setSlots(res.data ?? []))
            .catch((err) => setError(err.message))
            .finally(() => setLoadingSlots(false));
    };

    const onDateChange = (e) => {
        const d = e.target.value;
        setDate(d);
        loadSlots(d);
    };

    const confirmBooking = async () => {
        if (!asignatura || !date || !selectedSlot) {
            Swal.fire({
                icon: "info",
                title: "Faltan datos",
                text: "Selecciona la asignatura, la fecha y un horario disponible."
            });
            return;
        }
        setBooking(true);
        try {
            const res = await bookCita({
                monitorId: Number(monitorId),
                asignaturaId: Number(asignatura),
                fecha: date,
                horaInicio: selectedSlot
            });
            await Swal.fire({
                icon: "success",
                title: "Cita reservada",
                text: res.message || "Tu cita quedó reservada. El monitor debe confirmarla."
            });
            navigate("/citas");
        } catch (err) {
            // 409 => el horario dejó de estar disponible: refrescamos la lista
            if (err.status === 409) {
                loadSlots(date);
            }
            Swal.fire({
                icon: err.status === 409 ? "warning" : "error",
                title: err.status === 409 ? "Horario no disponible" : "No se pudo agendar",
                text: err.message
            });
        } finally {
            setBooking(false);
        }
    };

    if (loading) {
        return (
            <div className="text-center mt-16">
                <Spinner large />
            </div>
        );
    }

    return (
        <>
            <div className="page-head">
                <h1>Agendar cita</h1>
                <p>
                    Reserva una monitoría con {monitor?.nombre ?? "el monitor"}. Elige la
                    temática, la fecha y un horario disponible.
                </p>
            </div>

            <Alert type="error">{error}</Alert>

            {monitor && (
                <section className="card" style={{ maxWidth: 640 }}>
                    <div className="monitor-card__head">
                        <span className="monitor-card__av">{monitor.iniciales}</span>
                        <div>
                            <div className="monitor-card__name">{monitor.nombre}</div>
                            <div className="monitor-card__prog">{monitor.correo}</div>
                        </div>
                    </div>

                    <hr className="divider" />

                    <div className="field">
                        <label htmlFor="asignatura">Asignatura / temática</label>
                        <select
                            id="asignatura"
                            value={asignatura}
                            onChange={(e) => setAsignatura(e.target.value)}
                        >
                            <option value="">Selecciona una asignatura…</option>
                            {monitorSubjects.map((s) => (
                                <option key={s.id} value={s.id}>
                                    {s.nombre}
                                </option>
                            ))}
                        </select>
                        {monitorSubjects.length === 0 && (
                            <span className="muted">
                                Este monitor aún no ha registrado asignaturas.
                            </span>
                        )}
                    </div>

                    <div className="field">
                        <label htmlFor="fecha-cita">Fecha</label>
                        <input
                            id="fecha-cita"
                            type="date"
                            min={todayISO()}
                            value={date}
                            onChange={onDateChange}
                        />
                    </div>

                    <div className="field">
                        <label>Horarios disponibles</label>
                        {!date ? (
                            <span className="muted">Selecciona una fecha para ver los horarios.</span>
                        ) : loadingSlots ? (
                            <Spinner />
                        ) : slots.length === 0 ? (
                            <Alert type="info">
                                No hay horarios disponibles para esa fecha. Prueba con otra.
                            </Alert>
                        ) : (
                            <div className="chips">
                                {slots.map((s) => (
                                    <button
                                        key={s.horaInicio}
                                        type="button"
                                        className={`chip ${
                                            selectedSlot === hhmm(s.horaInicio)
                                                ? ""
                                                : "chip--muted"
                                        }`}
                                        onClick={() => setSelectedSlot(hhmm(s.horaInicio))}
                                    >
                                        {hhmm(s.horaInicio)} – {hhmm(s.horaFin)}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>

                    <div className="mt-16">
                        <button
                            type="button"
                            className="btn btn-primary"
                            onClick={confirmBooking}
                            disabled={booking || !asignatura || !selectedSlot}
                        >
                            {booking ? <Spinner /> : "Confirmar reserva"}
                        </button>
                        <button
                            type="button"
                            className="btn btn-ghost ml-8"
                            onClick={() => navigate("/monitores")}
                        >
                            Volver
                        </button>
                    </div>
                </section>
            )}
        </>
    );
}
