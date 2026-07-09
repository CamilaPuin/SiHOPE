import { useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Isotipo, { Wordmark } from "../../components/layout/Isotipo";
import Alert from "../../components/common/Alert";
import { PageLoader } from "../../components/common/Spinner";
import { verificar } from "../../services/registroService";

export default function Verificar() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token") ?? "";
    // Estado inicial derivado: sin token ya sabemos que el enlace es inválido,
    // así evitamos un setState síncrono dentro del efecto.
    const [estado, setEstado] = useState(token ? "cargando" : "error"); // cargando | ok | error
    const [mensaje, setMensaje] = useState(
        token ? "" : "El enlace de verificación no incluye un token válido."
    );
    const yaEjecutado = useRef(false);

    useEffect(() => {
        if (!token || yaEjecutado.current) return; // evita doble llamada en StrictMode
        yaEjecutado.current = true;

        verificar(token)
            .then((res) => {
                setEstado("ok");
                setMensaje(res.message || "Tu cuenta fue verificada correctamente.");
            })
            .catch((err) => {
                setEstado("error");
                setMensaje(
                    err.message ||
                        "El enlace de verificación no es válido o ya fue utilizado."
                );
            });
    }, [token]);

    if (estado === "cargando") {
        return <PageLoader mensaje="Verificando tu cuenta…" />;
    }

    return (
        <div className="auth-wrap">
            <div className="auth-card" style={{ maxWidth: 480 }}>
                <div className="auth-card__head">
                    <Isotipo negativo />
                    <Wordmark negativo />
                </div>

                <div className="auth-card__body">
                    <h1 className="font-title">Verificación de cuenta</h1>

                    {estado === "ok" ? (
                        <>
                            <Alert tipo="success">{mensaje}</Alert>
                            <Link
                                to="/login?verificado"
                                className="btn btn-primary btn-block mt-16"
                            >
                                Iniciar sesión
                            </Link>
                        </>
                    ) : (
                        <>
                            <Alert tipo="error">{mensaje}</Alert>
                            <Link to="/login" className="btn btn-ghost btn-block mt-16">
                                Volver a iniciar sesión
                            </Link>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}
