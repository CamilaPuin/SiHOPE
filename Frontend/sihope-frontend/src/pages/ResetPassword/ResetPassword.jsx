import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import Isotype, { Wordmark } from "../../components/layout/Isotype";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import PasswordRequirements from "../../components/common/PasswordRequirements";
import { reset } from "../../services/credentialsService";
import { isPasswordValid } from "../../utils/password";

export default function ResetPassword() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token") ?? "";

    const [nueva, setNueva] = useState("");
    const [nueva2, setNueva2] = useState("");
    const [error, setError] = useState("");
    const [matchError, setMatchError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setMatchError("");

        if (!isPasswordValid(nueva)) {
            setError("La contraseña no cumple los requisitos de seguridad.");
            return;
        }
        if (nueva !== nueva2 || !nueva2) {
            setMatchError("Las contraseñas no coinciden.");
            return;
        }

        setSubmitting(true);
        try {
            await reset({ token, nueva, nueva2 });
            navigate("/login?reestablecida");
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
                    <Isotype negative />
                    <Wordmark negative />
                </div>

                <div className="auth-card__body">
                    <h1 className="font-title">Restablecer contraseña</h1>
                    <p className="subtitle">Define una nueva contraseña para tu cuenta.</p>

                    {!token && (
                        <Alert type="error">
                            El enlace no es válido: falta el token de recuperación.
                        </Alert>
                    )}
                    <Alert type="error">{error}</Alert>

                    <form onSubmit={handleSubmit} noValidate>
                        <Field
                            label="Nueva contraseña"
                            id="nueva"
                            type="password"
                            value={nueva}
                            onChange={(e) => setNueva(e.target.value)}
                        />
                        <Field
                            label="Confirmar nueva contraseña"
                            id="nueva2"
                            type="password"
                            value={nueva2}
                            onChange={(e) => setNueva2(e.target.value)}
                            error={matchError}
                        />

                        <PasswordRequirements value={nueva} />

                        <button
                            type="submit"
                            className="btn btn-primary btn-block mt-16"
                            disabled={submitting || !token}
                        >
                            {submitting ? <Spinner /> : "Guardar nueva contraseña"}
                        </button>
                    </form>

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
