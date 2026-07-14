import { useEffect, useState, useCallback } from "react";
import Swal from "sweetalert2";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { listOpen, apply } from "../../services/vacancyService";

const APPLICATION_FIELDS = [
    {
        key: "promedio",
        label: "Promedio acumulado",
        type: "number",
        required: true,
        placeholder: "4.2",
        inputMode: "decimal",
        step: "0.01",
        min: "0"
    },
    {
        key: "semestre",
        label: "Semestre actual",
        type: "number",
        required: true,
        placeholder: "6",
        inputMode: "numeric",
        step: "1",
        min: "1"
    },
    {
        key: "motivacion",
        label: "¿Por qué quieres ser monitor?",
        type: "textarea",
        required: false,
        placeholder: "Cuéntanos brevemente tu motivación…"
    }
];

const NUMERIC_FIELDS = new Set(["promedio", "semestre"]);
const NUMERIC_VALIDATORS = {
    promedio: (value) => /^\d+(\.\d+)?$/.test(value),
    semestre: (value) => /^\d+$/.test(value)
};

const initialFormState = () =>
    Object.fromEntries(APPLICATION_FIELDS.map((c) => [c.key, ""]));

export default function Vacancies() {
    const [vacancies, setVacancies] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [selected, setSelected] = useState(null);
    const [form, setForm] = useState(initialFormState());
    const [errors, setErrors] = useState({});
    const [submitError, setSubmitError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const load = useCallback(async () => {
        try {
            const res = await listOpen();
            setVacancies(res.data ?? []);
            setLoadError("");
        } catch (err) {
            setLoadError(err.message);
        }
    }, []);

    useEffect(() => {
        let active = true;
        listOpen()
            .then((res) => {
                if (active) {
                    setVacancies(res.data ?? []);
                    setLoadError("");
                }
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
    }, []);

    const openModal = (vacancy) => {
        setSelected(vacancy);
        setForm(initialFormState());
        setErrors({});
        setSubmitError("");
    };

    const closeModal = () => setSelected(null);

    const update = (key) => (e) => {
        const value = e.target.value;

        if (NUMERIC_FIELDS.has(key)) {
            const validator = NUMERIC_VALIDATORS[key];
            const isValid = value === "" || validator(value);
            if (!isValid) {
                return;
            }
        }

        setForm((prev) => ({ ...prev, [key]: value }));
    };

    const submit = async (e) => {
        e.preventDefault();
        setSubmitError("");

        const missing = {};
        const invalid = {};
        APPLICATION_FIELDS.forEach((c) => {
            const value = form[c.key]?.trim() ?? "";

            if (c.required && !value) {
                missing[c.key] = "Este campo es obligatorio.";
            }

            if (NUMERIC_FIELDS.has(c.key) && value && !NUMERIC_VALIDATORS[c.key](value)) {
                invalid[c.key] = "Este campo solo acepta números.";
            }
        });

        const nextErrors = { ...missing, ...invalid };
        if (Object.keys(nextErrors).length > 0) {
            setErrors(nextErrors);
            return;
        }

        setSubmitting(true);
        try {
            const res = await apply(selected.id, form);
            closeModal();
            await load();
            Swal.fire({
                icon: "success",
                title: "¡Postulación registrada!",
                text: res.message || "Tu postulación se registró correctamente.",
                confirmButtonColor: "#e0a80d"
            });
        } catch (err) {
            if (err.data && typeof err.data === "object" && !Array.isArray(err.data)) {
                setErrors(err.data);
            }
            setSubmitError(err.message);
        } finally {
            setSubmitting(false);
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

            <Alert type="error">{loadError}</Alert>

            {loading ? (
                <div className="text-center mt-16">
                    <Spinner large />
                </div>
            ) : vacancies.length === 0 ? (
                <div className="card">
                    <p className="muted">
                        No hay convocatorias abiertas en este momento. Vuelve a consultar más
                        adelante.
                    </p>
                </div>
            ) : (
                <section className="grid grid--2">
                    {vacancies.map((c) => (
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
                                onClick={() => openModal(c)}
                            >
                                Postularme
                            </button>
                        </article>
                    ))}
                </section>
            )}

            {selected && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div
                        className="modal-box card"
                        onClick={(e) => e.stopPropagation()}
                        role="dialog"
                        aria-modal="true"
                    >
                        <div className="card__title">Postularme · {selected.titulo}</div>
                        <div className="card__subtitle">{selected.materia}</div>

                        <Alert type="error">{submitError}</Alert>

                        <form onSubmit={submit} noValidate>
                            {APPLICATION_FIELDS.map((field) =>
                                field.type === "textarea" ? (
                                    <Field
                                        key={field.key}
                                        label={field.label}
                                        id={field.key}
                                        error={errors[field.key]}
                                    >
                                        <textarea
                                            id={field.key}
                                            rows={3}
                                            value={form[field.key]}
                                            onChange={update(field.key)}
                                            placeholder={field.placeholder}
                                        />
                                    </Field>
                                ) : (
                                    <Field
                                        key={field.key}
                                        label={field.label}
                                        id={field.key}
                                        value={form[field.key]}
                                        onChange={update(field.key)}
                                        placeholder={field.placeholder}
                                        error={errors[field.key]}
                                        {...field}
                                    />
                                )
                            )}

                            <div className="modal-actions">
                                <button
                                    type="button"
                                    className="btn btn-ghost"
                                    onClick={closeModal}
                                    disabled={submitting}
                                >
                                    Cancelar
                                </button>
                                <button
                                    type="submit"
                                    className="btn btn-primary"
                                    disabled={submitting}
                                >
                                    {submitting ? <Spinner /> : "Enviar postulación"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </>
    );
}
