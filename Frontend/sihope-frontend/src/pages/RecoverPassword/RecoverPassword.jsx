import { useState } from "react";
import { Link } from "react-router-dom";
import Isotype from "../../components/layout/Isotype";
import logo from "../../images/logo-sihope.jpeg";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { recover } from "../../services/credentialsService";
import { UPTC_EMAIL } from "../../utils/password";

export default function RecoverPassword() {
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");
    const [sent, setSent] = useState(false);
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        if (!UPTC_EMAIL.test(email.trim())) {
            setError("Debes usar un correo institucional de la UPTC (@uptc.edu.co).");
            return;
        }
        setSubmitting(true);
        try {
            await recover(email.trim());
            setSent(true);
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="auth-wrap">
            <div className="auth-card" style={{ maxWidth: 480 }}>
                <div className="auth-card__head">
                    <Isotype src={logo} negative />
                </div>

                <div className="auth-card__body">
                    <h1 className="font-title">Recuperar contraseña</h1>
                    <p className="subtitle">
                        Ingresa tu correo institucional y te enviaremos un enlace temporal
                        para restablecer tu contraseña.
                    </p>

                    {sent ? (
                        <Alert type="success">
                            Si el correo está registrado, recibirás un enlace de
                            recuperación válido por 30 minutos.
                        </Alert>
                    ) : (
                        <form onSubmit={handleSubmit} noValidate>
                            <Alert type="error">{error}</Alert>
                            <Field
                                label="Correo institucional"
                                id="correo"
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                placeholder="nombre.apellido@uptc.edu.co"
                            />
                            <Alert type="info" className="mt-8">
                                ⓘ El enlace de recuperación tendrá una vigencia de{" "}
                                <strong>30 minutos</strong>.
                            </Alert>
                            <button
                                type="submit"
                                className="btn btn-primary btn-block"
                                disabled={submitting}
                            >
                                {submitting ? <Spinner /> : "Enviar enlace de recuperación"}
                            </button>
                        </form>
                    )}

                    <p className="auth-foot">
                        <Link className="link-strong" to="/login">
                            Volver a iniciar sesión
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
