import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import Swal from "sweetalert2";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useAuth } from "../../hooks/useAuth";
import {
    myCitas,
    confirmCita,
    cancelCita,
    attendCita
} from "../../services/citaService";
import { hhmm } from "../../utils/schedule";

const STATUS_BADGE = {
    RESERVADA: "badge-yellow",
    CONFIRMADA: "badge-active",
    ATENDIDA: "badge-role",
    CANCELADA: "badge-inactive"
};

const isUpcoming = (c) =>
    ["RESERVADA", "CONFIRMADA"].includes(c.estado) &&
    new Date(`${c.fecha}T${c.horaInicio}`) >= new Date();

export default function MyAppointments() {
    const { user } = useAuth();
    const [citas, setCitas] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [busy, setBusy] = useState(null);

    const load = useCallback(() => {
        setLoading(true);
        myCitas()
            .then((res) => setCitas(res.data ?? []))
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false));
    }, []);

    useEffect(() => {
        load();
    }, [load]);

    const { upcoming, history } = useMemo(() => {
        const up = [];
        const hist = [];
        for (const c of citas) (isUpcoming(c) ? up : hist).push(c);
        up.reverse(); // más próximas primero
        return { upcoming: up, history: hist };
    }, [citas]);

    const run = async (id, action, fn) => {
        setBusy(`${id}:${action}`);
        try {
            const res = await fn();
            await Swal.fire({
                toast: true,
                position: "top-end",
                showConfirmButton: false,
                timer: 2400,
                icon: "success",
                title: res.message || "Listo."
            });
            load();
        } catch (err) {
            Swal.fire({ icon: "error", title: "No se pudo completar", text: err.message });
        } finally {
            setBusy(null);
        }
    };

    const doCancel = async (c) => {
        const { value, isConfirmed } = await Swal.fire({
            title: "Cancelar cita",
            input: "textarea",
            inputLabel: "Motivo (opcional)",
            inputPlaceholder: "Ej: conflicto de horario",
            inputAttributes: {
                rows: 4,
                maxlength: 250
            },
            showCancelButton: true,
            confirmButtonText: "Cancelar cita",
            cancelButtonText: "Volver"
        });
        if (!isConfirmed) return;
        run(c.id, "cancelar", () => cancelCita(c.id, value || ""));
    };

    const card = (c) => {
        const badge = STATUS_BADGE[c.estado] ?? "badge";
        const counterpart = c.esMonitor ? c.estudiante : c.monitor;
        const canCancel = ["RESERVADA", "CONFIRMADA"].includes(c.estado) && isUpcoming(c);
        return (
            <article key={c.id} className="card">
                <div className="schedule__row" style={{ borderBottom: "none" }}>
                    <strong>{c.asignatura}</strong>
                    <span className={`badge ${badge}`}>{c.estado}</span>
                </div>
                <div className="muted" style={{ fontSize: "0.9rem" }}>
                    {c.esMonitor ? "Estudiante" : "Monitor"}: {counterpart}
                </div>
                {c.tema && (
                    <div className="muted" style={{ fontSize: "0.9rem" }}>
                        <strong>Tema:</strong> {c.tema}
                    </div>
                )}
                <div className="muted" style={{ fontSize: "0.9rem" }}>
                    {c.fecha} · {hhmm(c.horaInicio)} – {hhmm(c.horaFin)}
                </div>
                {c.estado === "CANCELADA" && c.motivoCancelacion && (
                    <div className="muted" style={{ fontSize: "0.85rem" }}>
                        Motivo: {c.motivoCancelacion}
                    </div>
                )}

                <div className="mt-16">
                    {c.esMonitor && c.estado === "RESERVADA" && (
                        <button
                            type="button"
                            className="btn btn-primary btn-sm"
                            disabled={busy === `${c.id}:confirmar`}
                            onClick={() => run(c.id, "confirmar", () => confirmCita(c.id))}
                        >
                            Confirmar
                        </button>
                    )}
                    {c.esMonitor && c.estado === "CONFIRMADA" && (
                        <button
                            type="button"
                            className="btn btn-primary btn-sm"
                            disabled={busy === `${c.id}:atender`}
                            onClick={() => run(c.id, "atender", () => attendCita(c.id))}
                        >
                            Marcar atendida
                        </button>
                    )}
                    {canCancel && (
                        <button
                            type="button"
                            className="btn btn-ghost btn-sm ml-8"
                            disabled={busy === `${c.id}:cancelar`}
                            onClick={() => doCancel(c)}
                        >
                            Cancelar
                        </button>
                    )}
                </div>
            </article>
        );
    };

    return (
        <>
            <div className="page-head">
                <h1>Mis citas</h1>
                <p>
                    {user?.rol === "MONITOR"
                        ? "Monitorías que los estudiantes agendaron contigo."
                        : "Monitorías que agendaste con los monitores del programa."}
                </p>
            </div>

            <Alert type="error">{error}</Alert>

            {loading ? (
                <div className="text-center mt-16">
                    <Spinner large />
                </div>
            ) : citas.length === 0 ? (
                <Alert type="info">
                    Aún no tienes citas.{" "}
                    {user?.rol !== "MONITOR" && (
                        <Link to="/monitores">Agenda una con un monitor.</Link>
                    )}
                </Alert>
            ) : (
                <>
                    <h2 className="mt-16">Próximas ({upcoming.length})</h2>
                    {upcoming.length === 0 ? (
                        <p className="muted">No tienes citas próximas.</p>
                    ) : (
                        <section className="grid grid--auto">{upcoming.map(card)}</section>
                    )}

                    <h2 className="mt-24">Historial ({history.length})</h2>
                    {history.length === 0 ? (
                        <p className="muted">Sin citas anteriores.</p>
                    ) : (
                        <section className="grid grid--auto">{history.map(card)}</section>
                    )}
                </>
            )}
        </>
    );
}
