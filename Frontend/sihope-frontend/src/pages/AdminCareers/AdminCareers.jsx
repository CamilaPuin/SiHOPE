import { useCallback, useEffect, useState } from "react";
import Swal from "sweetalert2";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import {
    listCareers,
    createCareer,
    deleteCareer
} from "../../services/adminCareerService";

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

export default function AdminCareers() {
    const [careers, setCareers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState("");

    const [name, setName] = useState("");
    const [createError, setCreateError] = useState("");
    const [creating, setCreating] = useState(false);

    const load = useCallback(async () => {
        try {
            const res = await listCareers();
            setCareers(res.data ?? []);
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
            setCreateError("Escribe el nombre de la carrera.");
            return;
        }
        setCreating(true);
        try {
            const res = await createCareer(value);
            setName("");
            await load();
            toast("success", res.message || "Carrera registrada.");
        } catch (err) {
            const detail = Array.isArray(err.data) ? err.data.join(" ") : "";
            setCreateError(`${err.message} ${detail}`.trim());
        } finally {
            setCreating(false);
        }
    };

    const handleDelete = async (career) => {
        const confirmation = await Swal.fire({
            title: "¿Eliminar carrera?",
            html: `<strong>${career.nombre}</strong>`,
            icon: "warning",
            showCancelButton: true,
            confirmButtonText: "Eliminar",
            cancelButtonText: "Cancelar",
            confirmButtonColor: "#a12626"
        });
        if (!confirmation.isConfirmed) return;

        try {
            await deleteCareer(career.id);
            await load();
            toast("success", "Carrera eliminada.");
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
                <h1>Carreras</h1>
                <p>
                    Administra el catálogo de carreras de la institución. Al crear un
                    estudiante se le asociará una de las carreras registradas aquí.
                </p>
            </div>

            <section className="card" style={{ marginBottom: 28 }}>
                <div className="card__title">Registrar carrera</div>
                <div className="card__subtitle">
                    Añade una nueva carrera al catálogo. No se permiten nombres duplicados.
                </div>

                <Alert type="error">{createError}</Alert>

                <form onSubmit={handleCreate} noValidate>
                    <div className="toolbar mt-8">
                        <input
                            type="text"
                            className="grow"
                            placeholder="Ej: Ingeniería de Sistemas y Computación"
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
                    No se puede eliminar una carrera que tenga usuarios asociados.
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
                                    <th>Carrera</th>
                                    <th style={{ width: 140 }}>Acciones</th>
                                </tr>
                            </thead>
                            <tbody>
                                {careers.map((c) => (
                                    <tr key={c.id}>
                                        <td>
                                            <strong>{c.nombre}</strong>
                                        </td>
                                        <td>
                                            <button
                                                type="button"
                                                className="btn btn-sm btn-danger"
                                                onClick={() => handleDelete(c)}
                                            >
                                                Eliminar
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                {careers.length === 0 && (
                                    <tr>
                                        <td colSpan={2} className="text-center muted">
                                            El catálogo está vacío. Registra la primera
                                            carrera arriba.
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
