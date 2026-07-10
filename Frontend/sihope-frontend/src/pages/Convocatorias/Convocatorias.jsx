import { useEffect, useState, useCallback } from "react";
import Swal from "sweetalert2";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listarAbiertas, postular } from "../../services/convocatoriaService";

/**
 * Formulario de postulación PARAMETRIZABLE (mitigación de HU_005): los campos que
 * se piden al aspirante se declaran aquí. Ajustar este arreglo cambia el formulario
 * sin refactorizar el componente ni el backend (que los guarda como JSON).
 */
const CAMPOS_POSTULACION = [
    { clave: "promedio", etiqueta: "Promedio acumulado", tipo: "text", requerido: true, placeholder: "4.2" },
    { clave: "semestre", etiqueta: "Semestre actual", tipo: "text", requerido: true, placeholder: "6" },
    {
        clave: "motivacion",
        etiqueta: "¿Por qué quieres ser monitor?",
        tipo: "textarea",
        requerido: false,
        placeholder: "Cuéntanos brevemente tu motivación…"
    }
];

const estadoInicialForm = () =>
    Object.fromEntries(CAMPOS_POSTULACION.map((c) => [c.clave, ""]));

export default function Convocatorias() {
    const [convocatorias, setConvocatorias] = useState([]);
    const [cargando, setCargando] = useState(true);
    const [errorCarga, setErrorCarga] = useState("");

    const [seleccionada, setSeleccionada] = useState(null); // convocatoria en el modal
    const [form, setForm] = useState(estadoInicialForm());
    const [errores, setErrores] = useState({});
    const [errorEnvio, setErrorEnvio] = useState("");
    const [enviando, setEnviando] = useState(false);

    const cargar = useCallback(async () => {
        try {
            const res = await listarAbiertas();
            setConvocatorias(res.data ?? []);
            setErrorCarga("");
        } catch (err) {
            setErrorCarga(err.message);
        } finally {
            setCargando(false);
        }
    }, []);

    useEffect(() => {
        cargar();
    }, [cargar]);

    const abrirModal = (convocatoria) => {
        setSeleccionada(convocatoria);
        setForm(estadoInicialForm());
        setErrores({});
        setErrorEnvio("");
    };

    const cerrarModal = () => setSeleccionada(null);

    const actualizar = (clave) => (e) =>
        setForm((prev) => ({ ...prev, [clave]: e.target.value }));

    const enviar = async (e) => {
        e.preventDefault();
        setErrorEnvio("");

        const faltantes = {};
        CAMPOS_POSTULACION.forEach((c) => {
            if (c.requerido && !form[c.clave]?.trim()) {
                faltantes[c.clave] = "Este campo es obligatorio.";
            }
        });
        if (Object.keys(faltantes).length > 0) {
            setErrores(faltantes);
            return;
        }

        setEnviando(true);
        try {
            const res = await postular(seleccionada.id, form);
            cerrarModal();
            await cargar();
            Swal.fire({
                icon: "success",
                title: "¡Postulación registrada!",
                text: res.message || "Tu postulación se registró correctamente.",
                confirmButtonColor: "#e0a80d"
            });
        } catch (err) {
            if (err.data && typeof err.data === "object" && !Array.isArray(err.data)) {
                setErrores(err.data);
            }
            setErrorEnvio(err.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <>
            <div className="page-head">
                <h1>Convocatorias abiertas</h1>
                <p>
                    Revisa las convocatorias de monitoría vigentes y postúlate adjuntando
                    la información solicitada.
                </p>
            </div>

            <Alert tipo="error">{errorCarga}</Alert>

            {cargando ? (
                <div className="text-center mt-16">
                    <Spinner grande />
                </div>
            ) : convocatorias.length === 0 ? (
                <div className="card">
                    <p className="muted">
                        No hay convocatorias abiertas en este momento. Vuelve a consultar más
                        adelante.
                    </p>
                </div>
            ) : (
                <section className="grid grid--2">
                    {convocatorias.map((c) => (
                        <article key={c.id} className="card">
                            <div className="card__title">{c.titulo}</div>
                            <div className="card__subtitle">{c.materia}</div>

                            {c.descripcion && <p>{c.descripcion}</p>}

                            <div className="schedule">
                                <div className="schedule__row">
                                    <span>Plazas disponibles</span>
                                    <span className="badge badge-yellow">{c.plazas}</span>
                                </div>
                                <div className="schedule__row">
                                    <span>Fecha límite</span>
                                    <span className="badge badge-yellow">{c.fechaLimite}</span>
                                </div>
                            </div>

                            {c.requisitos && (
                                <p className="muted" style={{ marginTop: 12 }}>
                                    <strong>Requisitos:</strong> {c.requisitos}
                                </p>
                            )}

                            <button
                                type="button"
                                className="btn btn-primary btn-sm mt-16"
                                onClick={() => abrirModal(c)}
                            >
                                Postularme
                            </button>
                        </article>
                    ))}
                </section>
            )}

            {seleccionada && (
                <div className="modal-overlay" onClick={cerrarModal}>
                    <div
                        className="modal-box card"
                        onClick={(e) => e.stopPropagation()}
                        role="dialog"
                        aria-modal="true"
                    >
                        <div className="card__title">Postularme · {seleccionada.titulo}</div>
                        <div className="card__subtitle">{seleccionada.materia}</div>

                        <Alert tipo="error">{errorEnvio}</Alert>

                        <form onSubmit={enviar} noValidate>
                            {CAMPOS_POSTULACION.map((campo) =>
                                campo.tipo === "textarea" ? (
                                    <Field
                                        key={campo.clave}
                                        label={campo.etiqueta}
                                        id={campo.clave}
                                        error={errores[campo.clave]}
                                    >
                                        <textarea
                                            id={campo.clave}
                                            rows={3}
                                            value={form[campo.clave]}
                                            onChange={actualizar(campo.clave)}
                                            placeholder={campo.placeholder}
                                        />
                                    </Field>
                                ) : (
                                    <Field
                                        key={campo.clave}
                                        label={campo.etiqueta}
                                        id={campo.clave}
                                        value={form[campo.clave]}
                                        onChange={actualizar(campo.clave)}
                                        placeholder={campo.placeholder}
                                        error={errores[campo.clave]}
                                    />
                                )
                            )}

                            <div className="modal-actions">
                                <button
                                    type="button"
                                    className="btn btn-ghost"
                                    onClick={cerrarModal}
                                    disabled={enviando}
                                >
                                    Cancelar
                                </button>
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={enviando}
                                >
                                    {enviando ? <Spinner /> : "Enviar postulación"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </>
    );
}
