import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import Isotipo, { Wordmark } from "../../components/layout/Isotipo";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import PasswordRequirements from "../../components/common/PasswordRequirements";
import { restablecer } from "../../services/credencialesService";
import { passwordValida } from "../../utils/password";

export default function Restablecer() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token") ?? "";

    const [nueva, setNueva] = useState("");
    const [nueva2, setNueva2] = useState("");
    const [error, setError] = useState("");
    const [errorMatch, setErrorMatch] = useState("");
    const [enviando, setEnviando] = useState(false);

    const manejarSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setErrorMatch("");

        if (!passwordValida(nueva)) {
            setError("La contraseña no cumple los requisitos de seguridad.");
            return;
        }
        if (nueva !== nueva2 || !nueva2) {
            setErrorMatch("Las contraseñas no coinciden.");
            return;
        }

        setEnviando(true);
        try {
            await restablecer({ token, nueva, nueva2 });
            navigate("/login?reestablecida");
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
                    <h1 className="font-title">Restablecer contraseña</h1>
                    <p className="subtitle">Define una nueva contraseña para tu cuenta.</p>

                    {!token && (
                        <Alert tipo="error">
                            El enlace no es válido: falta el token de recuperación.
                        </Alert>
                    )}
                    <Alert tipo="error">{error}</Alert>

                    <form onSubmit={manejarSubmit} noValidate>
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
                            error={errorMatch}
                        />

                        <PasswordRequirements valor={nueva} />

                        <button
                            type="submit"
                            className="btn btn-primary btn-block mt-16"
                            disabled={enviando || !token}
                        >
                            {enviando ? <Spinner /> : "Guardar nueva contraseña"}
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
