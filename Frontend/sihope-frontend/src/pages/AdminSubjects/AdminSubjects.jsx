import { useCallback, useEffect, useState } from "react";
import Swal from "sweetalert2";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import {
    listSubjects,
    createSubject,
    deleteSubject
} from "../../services/adminSubjectService";

const toast = (icon, title) =>
    Swal.fire({
        toast: true,
        position: "top-end",
        showConfirmButton: false,
        timer: 2600,
        timerProgressBar: true,
        icon,
        title
    });

export default function AdminSubjects() {
    const [subjects, setSubjects] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [name, setName] = useState("");
    const [createError, setCreateError] = useState("");
    const [creating, setCreating] = useState(false);

    const load = useCallback(async () => {
        try {
            const res = await listSubjects();
            setSubjects(res.data ?? []);
            setLoadError("");
        } catch (err) {
            setLoadError(err.message);
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        load();
    }, [load]);

    const handleCreate = async (e) => {
        e.preventDefault();
        setCreateError("");
        const value = name.trim();
        if (!value) {
            setCreateError("Escribe el nombre de la asignatura.");
            return;
        }
        setCreating(true);
        try {
            const res = await createSubject(value);
            setName("");
            await load();
            toast("success", res.message || "Asignatura registrada.");
        } catch (err) {
            const detail = Array.isArray(err.data) ? err.data.join(" ") : "";
            setCreateError(`${err.message} ${detail}`.trim());
        } finally {
            setCreating(false);
        }
    };

    const handleDelete = async (subject) => {
        const confirmation = await Swal.fire({
            title: "¿Eliminar asignatura?",
            html: `<strong>${subject.nombre}</strong>`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Eliminar",
            cancelButtonText: "Cancelar",
            confirmButtonColor: "#a12626"
        });
        if (!confirmation.isConfirmed) return;

        try {
            await deleteSubject(subject.id);
            await load();
            toast("success", "Asignatura eliminada.");
        } catch (err) {
            const detail = Array.isArray(err.data) ? err.data.join(" ") : "";
            Swal.fire({
                icon: "error",
                title: "No se pudo eliminar",
                text: `${err.message} ${detail}`.trim()
            });
        }
    };

    return (
        <>
            <div className="page-head">
                <h1>Asignaturas</h1>
                <p>
                    Administra el catálogo de asignaturas del programa. Los monitores solo
                    podrán ofrecer materias registradas aquí.
                </p>
            </div>

            <section className="card" style={{ marginBottom: 28 }}>
                <div className="card__title">Registrar asignatura</div>
                <div className="card__subtitle">
                    Añade una nueva asignatura al catálogo. No se permiten nombres duplicados.
                </div>

                <Alert type="error">{createError}</Alert>

                <form onSubmit={handleCreate} noValidate>
                    <div className="toolbar mt-8">
                        <input
                            type="text"
                            className="grow"
                            placeholder="Ej: Cálculo Diferencial"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                        />
                        <button
                            type="submit"
                            className="btn btn-primary"
                            disabled={creating}
                        >
                            {creating ? <Spinner /> : "Registrar"}
                        </button>
                    </div>
                </form>
            </section>

            <section className="card">
                <div className="card__title">Catálogo actual</div>
                <div className="card__subtitle">
                    No se puede eliminar una asignatura que algún monitor atienda o que tenga
                    citas asociadas.
                </div>

                <Alert type="error">{loadError}</Alert>

                {loading ? (
                    <div className="text-center mt-16">
                        <Spinner large />
                    </div>
                ) : (
                    <div className="table-wrap">
                        <table className="table">
                            <thead>
                                <tr>
                                    <th>Asignatura</th>
                                    <th style={{ width: 140 }}>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                {subjects.map((s) => (
                                    <tr key={s.id}>
                                        <td>
                                            <strong>{s.nombre}</strong>
                                        </td>
                                        <td>
                                            <button
                                                type="button"
                                                className="btn btn-sm btn-danger"
                                                onClick={() => handleDelete(s)}
                                            >
                                                Eliminar
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                {subjects.length === 0 && (
                                    <tr>
                                        <td colSpan={2} className="text-center muted">
                                            El catálogo está vacío. Registra la primera
                                            asignatura arriba.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>
        </>
    );
}
