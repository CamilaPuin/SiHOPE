import { useEffect, useState } from "react";
import { Link, useNavigate, useLocation, useSearchParams } from "react-router-dom";
import Isotype, { Wordmark } from "../../components/layout/Isotype";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useAuth } from "../../hooks/useAuth";
import { panelByRole } from "../../utils/roles";
import "./Login.css";

const QUERY_MESSAGES = {
    logout: { type: "success", text: "Cerraste sesión correctamente." },
    verified: {
        type: "success",
        text: "Tu cuenta fue verificada. Ya puedes iniciar sesión."
    },
    passwordReset: {
        type: "success",
        text: "Tu contraseña fue restablecida. Inicia sesión con la nueva."
    },
    registered: {
        type: "info",
        text:
            "¡Cuenta creada! Revisa tu correo institucional y verifícala antes de iniciar sesión."
    },
    invalidToken: {
        type: "error",
        text: "El enlace de verificación no es válido o ya fue utilizado."
    }
};

export default function Login() {
    const { signIn, isAuthenticated, user } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");

    useEffect(() => {
        if (isAuthenticated && user) {
            navigate(panelByRole(user.rol), { replace: true });
        }
    }, [isAuthenticated, user, navigate]);

    const messageKey = Object.keys(QUERY_MESSAGES).find((k) =>
        searchParams.has(k)
    );
    const queryMessage = messageKey ? QUERY_MESSAGES[messageKey] : null;

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSubmitting(true);
        try {
            const session = await signIn({ correo: email, password });
            const target = location.state?.from ?? panelByRole(session.rol);
            navigate(target, { replace: true });
        } catch (err) {
            setError(err.message);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="login-wrap">
            <main className="login-card">
                <section className="login-brand">
                    <div className="login-brand__logo">
                        <Isotype negative />
                        <Wordmark negative />
                    </div>
                    <div>
                        <p className="login-brand__headline">
                            Conecta el talento que <span>impulsa</span> tu aprendizaje.
                        </p>
                        <p className="login-brand__text">
                            Plataforma de monitorías académicas de la UPTC: agenda citas,
                            consulta disponibilidad y participa en las convocatorias de
                            monitores.
                        </p>
                    </div>
                    <p className="login-brand__tagline">Conectate · Edúcate · Superate</p>
                </section>

                <section className="login-form-wrap">
                    <h1 className="font-title">Iniciar sesión</h1>
                    <p className="subtitle">
                        Ingresa con tu cuenta institucional para continuar.
                    </p>

                    {queryMessage && (
                        <Alert type={queryMessage.type}>{queryMessage.text}</Alert>
                    )}
                    <Alert type="error">{error}</Alert>

                    <form onSubmit={handleSubmit} noValidate>
                        <Field
                            label="Correo institucional"
                            id="correo"
                            type="email"
                            name="correo"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            placeholder="nombre.apellido@uptc.edu.co"
                            autoComplete="username"
                            required
                        />
                        <Field
                            label="Contraseña"
                            id="password"
                            type="password"
                            name="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            autoComplete="current-password"
                            required
                        />

                        <div className="login-row">
                            <label className="login-remember">
                                <input type="checkbox" name="recordarme" /> Recordarme
                            </label>
                            <Link className="link-strong" to="/recuperar">
                                ¿Olvidaste tu contraseña?
                            </Link>
                        </div>

                        <button
                            type="submit"
                            className="btn btn-primary btn-block"
                            disabled={submitting}
                        >
                            {submitting ? <Spinner /> : "Entrar"}
                        </button>
                    </form>

                    <p className="login-foot">
                        ¿No tienes cuenta?{" "}
                        <Link className="link-strong" to="/registro">
                            Solicita acceso
                        </Link>
                    </p>
                </section>
            </main>
        </div>
    );
}
