import { useState } from "react";
import { Link } from "react-router-dom";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import PasswordRequirements from "../../components/common/PasswordRequirements";
import { changePassword } from "../../services/credentialsService";
import { isPasswordValid } from "../../utils/password";

const INITIAL = { actual: "", nueva: "", nueva2: "" };

export default function Credentials() {
    const [form, setForm] = useState(INITIAL);
    const [error, setError] = useState("");
    const [matchError, setMatchError] = useState("");
    const [success, setSuccess] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const update = (field) => (e) =>
        setForm((prev) => ({ ...prev, [field]: e.target.value }));

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setMatchError("");
        setSuccess("");

        if (!form.actual.trim()) {
            setError("Ingresa tu contraseña actual.");
            return;
        }
        if (!isPasswordValid(form.nueva)) {
            setError("La nueva contraseña no cumple los requisitos de seguridad.");
            return;
        }
        if (form.nueva !== form.nueva2 || !form.nueva2) {
            setMatchError("Las contraseñas nuevas no coinciden.");
            return;
        }

        setSubmitting(true);
        try {
            const res = await changePassword(form);
            setSuccess(res.message || "Tu contraseña fue actualizada correctamente.");
            setForm(INITIAL);
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <>
            <div className="page-head">
                <h1>Mis credenciales</h1>
                <p>
                    Actualiza tu contraseña. Por seguridad, necesitas confirmar tu
                    contraseña actual.
                </p>
            </div>

            <div className="grid grid--2">
                <section className="card">
                    <div className="card__title">Cambiar contraseña</div>
                    <div className="card__subtitle">
                        Tu nueva contraseña debe cumplir los requisitos de seguridad.
                    </div>

                    <Alert type="success">{success}</Alert>
                    <Alert type="error">{error}</Alert>

                    <form onSubmit={handleSubmit} noValidate>
                        <Field
                            label="Contraseña actual"
                            id="actual"
                            type="password"
                            value={form.actual}
                            onChange={update("actual")}
                        />
                        <Field
                            label="Nueva contraseña"
                            id="nueva"
                            type="password"
                            value={form.nueva}
                            onChange={update("nueva")}
                        />
                        <Field
                            label="Confirmar nueva contraseña"
                            id="nueva2"
                            type="password"
                            value={form.nueva2}
                            onChange={update("nueva2")}
                            error={matchError}
                        />

                        <PasswordRequirements value={form.nueva} />

                        <button
                            type="submit"
                            className="btn btn-primary mt-16"
                            disabled={submitting}
                        >
                            {submitting ? <Spinner /> : "Actualizar contraseña"}
                        </button>
                    </form>
                </section>

                <section className="card" style={{ alignSelf: "start" }}>
                    <div className="card__title">¿Olvidaste tu contraseña?</div>
                    <div className="card__subtitle">
                        Te enviamos un enlace temporal a tu correo institucional.
                    </div>
                    <p className="muted" style={{ fontSize: "0.92rem", lineHeight: 1.6 }}>
                        El enlace de recuperación tiene una vigencia de{" "}
                        <strong>30 minutos</strong>. Si tu sesión está activa, puedes
                        cambiar la contraseña directamente en el formulario de la izquierda.
                    </p>
                    <Link to="/recuperar" className="btn btn-ghost mt-16">
                        Ir a recuperación por correo
                    </Link>
                </section>
            </div>
        </>
    );
}
