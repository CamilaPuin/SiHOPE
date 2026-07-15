import { useEffect, useState } from "react";
import Swal from "sweetalert2";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { citasReport, downloadReport } from "../../services/reportService";
import { listMonitors } from "../../services/availabilityService";
import logo from "../../images/logo-sihope-fondoblanco.jpg";

// Formatea una fecha local como yyyy-MM-dd SIN pasar por UTC (toISOString convierte a UTC
// y en zonas con offset negativo, como Colombia UTC-5, puede devolver el día anterior).
const toLocalISO = (d) => {
    const y = d.getFullYear();
    const m = String(d.getMonth() + 1).padStart(2, "0");
    const day = String(d.getDate()).padStart(2, "0");
    return `${y}-${m}-${day}`;
};
const firstOfMonth = () => {
    const d = new Date();
    return toLocalISO(new Date(d.getFullYear(), d.getMonth(), 1));
};
const today = () => toLocalISO(new Date());

const formatDate = (iso) => {
    if (!iso) return "";
    const [y, m, d] = iso.split("-");
    return `${d}/${m}/${y}`;
};

export default function CoordinatorReports() {
    const [desde, setDesde] = useState(firstOfMonth());
    const [hasta, setHasta] = useState(today());
    const [monitorId, setMonitorId] = useState("");
    const [monitors, setMonitors] = useState([]);
    const [report, setReport] = useState(null);
    // Parámetros con los que se generó el reporte actualmente mostrado. Se "congelan" al
    // generar para que el encabezado y las descargas usen SIEMPRE el periodo consultado,
    // aunque el usuario cambie los inputs después.
    const [applied, setApplied] = useState(null);
    const [loading, setLoading] = useState(false);
    const [downloading, setDownloading] = useState("");
    const [error, setError] = useState("");

    useEffect(() => {
        listMonitors()
            .then((res) => setMonitors(res.data ?? []))
            .catch(() => setMonitors([]));
    }, []);

    const generate = async () => {
        if (desde > hasta) {
            setError("La fecha 'desde' no puede ser posterior a 'hasta'.");
            return;
        }
        setError("");
        setLoading(true);
        try {
            const monitor = monitorId || undefined;
            const res = await citasReport(desde, hasta, monitor);
            setReport(res.data);
            // Congelamos el periodo/monitor efectivamente consultados.
            setApplied({ desde, hasta, monitorId });
        } catch (err) {
            setError(err.message);
            setReport(null);
            setApplied(null);
        } finally {
            setLoading(false);
        }
    };

    const doDownload = async (formato) => {
        if (!applied) return;
        setDownloading(formato);
        try {
            await downloadReport(
                applied.desde,
                applied.hasta,
                formato,
                applied.monitorId || undefined
            );
        } catch (err) {
            Swal.fire({ icon: "error", title: "No se pudo descargar", text: err.message });
        } finally {
            setDownloading("");
        }
    };

    return (
        <>
            <div className="page-head">
                <h1>Reportes de monitorías</h1>
                <p>
                    Consulta las citas atendidas por periodo, con totales por monitor y por
                    tema, y expórtalas en PDF o Excel.
                </p>
            </div>

            <Alert type="error">{error}</Alert>

            <section className="card">
                <div className="toolbar">
                    <div className="field" style={{ margin: 0 }}>
                        <label htmlFor="desde">Desde</label>
                        <input
                            id="desde"
                            className="input"
                            type="date"
                            value={desde}
                            onChange={(e) => setDesde(e.target.value)}
                        />
                    </div>
                    <div className="field" style={{ margin: 0 }}>
                        <label htmlFor="hasta">Hasta</label>
                        <input
                            id="hasta"
                            className="input"
                            type="date"
                            value={hasta}
                            onChange={(e) => setHasta(e.target.value)}
                        />
                    </div>
                    <div className="field" style={{ margin: 0 }}>
                        <label htmlFor="monitor">Monitor</label>
                        <select
                            id="monitor"
                            className="input"
                            value={monitorId}
                            onChange={(e) => setMonitorId(e.target.value)}
                        >
                            <option value="">Todos los monitores</option>
                            {monitors.map((m) => (
                                <option key={m.id} value={m.id}>
                                    {m.nombre}
                                </option>
                            ))}
                        </select>
                    </div>
                    <button type="button" className="btn btn-primary" onClick={generate} disabled={loading}>
                        {loading ? <Spinner /> : "Generar reporte"}
                    </button>
                </div>
            </section>

            {report && (
                <>
                    <section className="report-brand mt-16">
                        <img
                            className="report-brand__logo"
                            src={logo}
                            alt="Logo de SiHope"
                        />
                        <div className="report-brand__meta">
                            <div className="report-brand__title">
                                Reporte de citas atendidas
                            </div>
                            <div className="report-brand__line">
                                <strong>Periodo:</strong> {formatDate(report.desde)} -{" "}
                                {formatDate(report.hasta)}
                            </div>
                            <div className="report-brand__line">
                                <strong>Monitor:</strong>{" "}
                                {applied?.monitorId
                                    ? monitors.find(
                                          (m) => String(m.id) === String(applied.monitorId)
                                      )?.nombre ?? "-"
                                    : "Todos los monitores"}
                            </div>
                        </div>
                        <span className="report-brand__badge">SiHope</span>
                    </section>

                    <section className="grid grid--3" style={{ margin: "20px 0" }}>
                        <div className="stat">
                            <div className="stat__label">Citas atendidas</div>
                            <div className="stat__value">{report.total}</div>
                            <div className="stat__accent" />
                        </div>
                        <div className="stat">
                            <div className="stat__label">Monitores con actividad</div>
                            <div className="stat__value">{report.porMonitor.length}</div>
                            <div className="stat__accent" />
                        </div>
                        <div className="stat">
                            <div className="stat__label">Temas atendidos</div>
                            <div className="stat__value">{report.porTema.length}</div>
                            <div className="stat__accent" />
                        </div>
                    </section>

                    {report.mensaje ? (
                        <Alert type="info">{report.mensaje}</Alert>
                    ) : (
                        <section className="grid grid--2">
                            <div className="card">
                                <div className="card__title">Por monitor</div>
                                <div className="schedule mt-16">
                                    {report.porMonitor.map((r) => (
                                        <div key={r.nombre} className="schedule__row">
                                            <span>{r.nombre}</span>
                                            <span className="badge badge-active">{r.total}</span>
                                        </div>
                                    ))}
                                </div>
                            </div>
                            <div className="card">
                                <div className="card__title">Por tema</div>
                                <div className="schedule mt-16">
                                    {report.porTema.map((r) => (
                                        <div key={r.nombre} className="schedule__row">
                                            <span>{r.nombre}</span>
                                            <span className="badge badge-yellow">{r.total}</span>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </section>
                    )}

                    {report.citas && report.citas.length > 0 && (
                        <section className="card mt-16">
                            <div className="card__title">Detalle de citas atendidas</div>
                            <div className="report-detail mt-16">
                                <div className="report-detail__row report-detail__row--head">
                                    <span>Estudiante</span>
                                    <span>Asignatura</span>
                                    <span>Fecha</span>
                                    <span>Hora</span>
                                </div>
                                {report.citas.map((c, i) => (
                                    <div
                                        key={`${c.estudiante}-${c.fecha}-${c.horaInicio}-${i}`}
                                        className="report-detail__row"
                                    >
                                        <span>{c.estudiante}</span>
                                        <span className="muted">{c.asignatura}</span>
                                        <span className="muted">{c.fecha}</span>
                                        <span className="muted">
                                            {c.horaInicio} - {c.horaFin}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </section>
                    )}

                    <div className="mt-16">
                        <button
                            type="button"
                            className="btn btn-ghost"
                            onClick={() => doDownload("pdf")}
                            disabled={downloading === "pdf"}
                        >
                            {downloading === "pdf" ? <Spinner /> : "Descargar PDF"}
                        </button>
                        <button
                            type="button"
                            className="btn btn-ghost ml-8"
                            onClick={() => doDownload("excel")}
                            disabled={downloading === "excel"}
                        >
                            {downloading === "excel" ? <Spinner /> : "Descargar Excel"}
                        </button>
                    </div>
                </>
            )}
        </>
    );
}
