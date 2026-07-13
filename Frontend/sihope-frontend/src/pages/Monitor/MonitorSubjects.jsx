import { useEffect, useMemo, useState } from "react";
import Swal from "sweetalert2";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import {
    listAsignaturas,
    mySubjects,
    saveMySubjects
} from "../../services/asignaturaService";

export default function MonitorSubjects() {
    const [catalog, setCatalog] = useState([]);
    const [subjects, setSubjects] = useState([]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        Promise.all([listAsignaturas(), mySubjects()])
            .then(([cat, mine]) => {
                if (!active) return;
                setCatalog((cat.data ?? []).map((a) => a.nombre));
                setSubjects(mine.data ?? []);
            })
            .catch((err) => {
                if (active) setError(err.message);
            })
            .finally(() => {
                if (active) setLoading(false);
            });
        return () => {
            active = false;
        };
    }, []);

    const suggestions = useMemo(() => {
        const taken = new Set(subjects.map((s) => s.toLowerCase()));
        return catalog.filter((c) => !taken.has(c.toLowerCase()));
    }, [catalog, subjects]);

    const add = (raw) => {
        const value = raw.trim();
        if (!value) return;
        if (subjects.some((s) => s.toLowerCase() === value.toLowerCase())) {
            setInput("");
            return;
        }
        setSubjects((prev) => [...prev, value]);
        setInput("");
    };

    const remove = (name) =>
        setSubjects((prev) => prev.filter((s) => s !== name));

    const onKeyDown = (e) => {
        if (e.key === "Enter") {
            e.preventDefault();
            add(input);
        }
    };

    const save = async () => {
        setSaving(true);
        try {
            const res = await saveMySubjects(subjects);
            Swal.fire({
                toast: true,
                position: "top-end",
                showConfirmButton: false,
                timer: 2600,
                timerProgressBar: true,
                icon: "success",
                title: res.message || "Asignaturas guardadas."
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
                <h1>Mis asignaturas</h1>
                <p>
                    Registra las temáticas que atiendes. Los estudiantes solo podrán agendar
                    citas contigo sobre estas asignaturas, y podrán filtrarte por ellas.
                </p>
            </div>

            <Alert type="error">{error}</Alert>

            <section className="card">
                {loading ? (
                    <div className="text-center mt-16">
                        <Spinner large />
                    </div>
                ) : (
                    <>
                        <div className="card__title">Asignaturas que atiendo</div>
                        <div className="card__subtitle">
                            Escribe una asignatura y presiona Enter, o elígela de las sugerencias.
                        </div>

                        <div className="chips mt-16">
                            {subjects.length === 0 ? (
                                <p className="muted">Aún no has agregado asignaturas.</p>
                            ) : (
                                subjects.map((s) => (
                                    <span key={s} className="chip">
                                        {s}
                                        <button
                                            type="button"
                                            className="chip__x"
                                            aria-label={`Quitar ${s}`}
                                            onClick={() => remove(s)}
                                        >
                                            ×
                                        </button>
                                    </span>
                                ))
                            )}
                        </div>

                        <div className="toolbar mt-16">
                            <input
                                type="text"
                                className="grow"
                                list="asignatura-sugerencias"
                                placeholder="Ej: Cálculo Diferencial"
                                value={input}
                                onChange={(e) => setInput(e.target.value)}
                                onKeyDown={onKeyDown}
                            />
                            <datalist id="asignatura-sugerencias">
                                {suggestions.map((s) => (
                                    <option key={s} value={s} />
                                ))}
                            </datalist>
                            <button
                                type="button"
                                className="btn btn-ghost"
                                onClick={() => add(input)}
                            >
                                Agregar
                            </button>
                        </div>

                        <div className="mt-16">
                            <button
                                type="button"
                                className="btn btn-primary"
                                onClick={save}
                                disabled={saving}
                            >
                                {saving ? <Spinner /> : "Guardar asignaturas"}
                            </button>
                        </div>
                    </>
                )}
            </section>
        </>
    );
}
