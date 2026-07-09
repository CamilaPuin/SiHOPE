import { useEffect, useState } from "react";
import { Link, useNavigate, useLocation, useSearchParams } from "react-router-dom";
import Isotipo, { Wordmark } from "../../components/layout/Isotipo";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import { useAuth } from "../../hooks/useAuth";
import { panelPorRol } from "../../utils/roles";
import "./Login.css";

/** Mensajes informativos que llegan por query-string desde otros flujos. */
const MENSAJES_QUERY = {
    logout: { tipo: "success", texto: "Cerraste sesión correctamente." },
    verificado: {
        tipo: "success",
        texto: "Tu cuenta fue verificada. Ya puedes iniciar sesión."
    },
    reestablecida: {
        tipo: "success",
        texto: "Tu contraseña fue restablecida. Inicia sesión con la nueva."
    },
    registrado: {
        tipo: "info",
        texto:
            "¡Cuenta creada! Revisa tu correo institucional y verifícala antes de iniciar sesión."
    },
    tokeninvalido: {
        tipo: "error",
        texto: "El enlace de verificación no es válido o ya fue utilizado."
    }
};

export default function Login() {
    const { iniciarSesion, estaAutenticado, usuario } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const [searchParams] = useSearchParams();

    const [correo, setCorreo] = useState("");
    const [password, setPassword] = useState("");
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState("");

    // Si ya hay sesión activa, no mostramos el login.
    useEffect(() => {
        if (estaAutenticado && usuario) {
            navigate(panelPorRol(usuario.rol), { replace: true });
        }
    }, [estaAutenticado, usuario, navigate]);

    // Banner informativo según el query (?logout, ?verificado, …).
    const claveMensaje = Object.keys(MENSAJES_QUERY).find((k) =>
        searchParams.has(k)
    );
    const mensajeQuery = claveMensaje ? MENSAJES_QUERY[claveMensaje] : null;

    const manejarSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setEnviando(true);
        try {
            const sesion = await iniciarSesion({ correo, password });
            const destino = location.state?.desde ?? panelPorRol(sesion.rol);
            navigate(destino, { replace: true });
        } catch (err) {
            setError(err.message);
        } finally {
            setEnviando(false);
        }
    };

    return (
        <div className="login-wrap">
            <main className="login-card">
                {/* Panel de marca */}
                <section className="login-brand">
                    <div className="login-brand__logo">
                        <Isotipo negativo />
                        <Wordmark negativo />
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
                    <p className="login-brand__tagline">Connect · Educate · Empower</p>
                </section>

                {/* Panel del formulario */}
                <section className="login-form-wrap">
                    <h1 className="font-title">Iniciar sesión</h1>
                    <p className="subtitle">
                        Ingresa con tu cuenta institucional para continuar.
                    </p>

                    {mensajeQuery && (
                        <Alert tipo={mensajeQuery.tipo}>{mensajeQuery.texto}</Alert>
                    )}
                    <Alert tipo="error">{error}</Alert>

                    <form onSubmit={manejarSubmit} noValidate>
                        <Field
                            label="Correo institucional"
                            id="correo"
                            type="email"
                            name="correo"
                            value={correo}
                            onChange={(e) => setCorreo(e.target.value)}
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
                            disabled={enviando}
                        >
                            {enviando ? <Spinner /> : "Entrar"}
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
