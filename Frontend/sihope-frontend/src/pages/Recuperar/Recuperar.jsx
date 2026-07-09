import { useState } from "react";
import { Link } from "react-router-dom";
import Isotipo, { Wordmark } from "../../components/layout/Isotipo";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { recuperar } from "../../services/credencialesService";
import { CORREO_UPTC } from "../../utils/password";

export default function Recuperar() {
    const [correo, setCorreo] = useState("");
    const [error, setError] = useState("");
    const [enviado, setEnviado] = useState(false);
    const [enviando, setEnviando] = useState(false);

    const manejarSubmit = async (e) => {
        e.preventDefault();
        setError("");
        if (!CORREO_UPTC.test(correo.trim())) {
            setError("Debes usar un correo institucional de la UPTC (@uptc.edu.co).");
            return;
        }
        setEnviando(true);
        try {
            await recuperar(correo.trim());
            // El backend siempre responde genérico (no revela si el correo existe).
            setEnviado(true);
        } catch (err) {
            setError(err.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <div className="auth-wrap">
            <div className="auth-card" style={{ maxWidth: 480 }}>
                <div className="auth-card__head">
                    <Isotipo negativo />
                    <Wordmark negativo />
                </div>

                <div className="auth-card__body">
                    <h1 className="font-title">Recuperar contraseña</h1>
                    <p className="subtitle">
                        Ingresa tu correo institucional y te enviaremos un enlace temporal
                        para restablecer tu contraseña.
                    </p>

                    {enviado ? (
                        <Alert tipo="success">
                            Si el correo está registrado, recibirás un enlace de
                            recuperación válido por 30 minutos.
                        </Alert>
                    ) : (
                        <form onSubmit={manejarSubmit} noValidate>
                            <Alert tipo="error">{error}</Alert>
                            <Field
                                label="Correo institucional"
                                id="correo"
                                type="email"
                                value={correo}
                                onChange={(e) => setCorreo(e.target.value)}
                                placeholder="nombre.apellido@uptc.edu.co"
                            />
                            <Alert tipo="info" className="mt-8">
                                ⓘ El enlace de recuperación tendrá una vigencia de{" "}
                                <strong>30 minutos</strong>.
                            </Alert>
                            <button
                                type="submit"
                                className="btn btn-primary btn-block"
                                disabled={enviando}
                            >
                                {enviando ? <Spinner /> : "Enviar enlace de recuperación"}
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
