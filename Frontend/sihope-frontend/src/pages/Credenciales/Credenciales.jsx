import { useState } from "react";
import { Link } from "react-router-dom";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import PasswordRequirements from "../../components/common/PasswordRequirements";
import { cambiarPassword } from "../../services/credencialesService";
import { passwordValida } from "../../utils/password";

const INICIAL = { actual: "", nueva: "", nueva2: "" };

export default function Credenciales() {
    const [form, setForm] = useState(INICIAL);
    const [error, setError] = useState("");
    const [errorMatch, setErrorMatch] = useState("");
    const [exito, setExito] = useState("");
    const [enviando, setEnviando] = useState(false);

    const actualizar = (campo) => (e) =>
        setForm((prev) => ({ ...prev, [campo]: e.target.value }));

    const manejarSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setErrorMatch("");
        setExito("");

        if (!form.actual.trim()) {
            setError("Ingresa tu contraseña actual.");
            return;
        }
        if (!passwordValida(form.nueva)) {
            setError("La nueva contraseña no cumple los requisitos de seguridad.");
            return;
        }
        if (form.nueva !== form.nueva2 || !form.nueva2) {
            setErrorMatch("Las contraseñas nuevas no coinciden.");
            return;
        }

        setEnviando(true);
        try {
            const res = await cambiarPassword(form);
            setExito(res.message || "Tu contraseña fue actualizada correctamente.");
            setForm(INICIAL);
        } catch (err) {
            setError(err.message);
        } finally {
            setEnviando(false);
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
                {/* Cambio de contraseña */}
                <section className="card">
                    <div className="card__title">Cambiar contraseña</div>
                    <div className="card__subtitle">
                        Tu nueva contraseña debe cumplir los requisitos de seguridad.
                    </div>

                    <Alert tipo="success">{exito}</Alert>
                    <Alert tipo="error">{error}</Alert>

                    <form onSubmit={manejarSubmit} noValidate>
                        <Field
                            label="Contraseña actual"
                            id="actual"
                            type="password"
                            value={form.actual}
                            onChange={actualizar("actual")}
                        />
                        <Field
                            label="Nueva contraseña"
                            id="nueva"
                            type="password"
                            value={form.nueva}
                            onChange={actualizar("nueva")}
                        />
                        <Field
                            label="Confirmar nueva contraseña"
                            id="nueva2"
                            type="password"
                            value={form.nueva2}
                            onChange={actualizar("nueva2")}
                            error={errorMatch}
                        />

                        <PasswordRequirements valor={form.nueva} />

                        <button
                            type="submit"
                            className="btn btn-primary mt-16"
                            disabled={enviando}
                        >
                            {enviando ? <Spinner /> : "Actualizar contraseña"}
                        </button>
                    </form>
                </section>

                {/* Recuperación */}
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
