import { useEffect, useRef, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Isotype, { Wordmark } from "../../components/layout/Isotype";
import Alert from "../../components/common/Alert";
import { PageLoader } from "../../components/common/Spinner";
import { verify } from "../../services/registrationService";

export default function Verify() {
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token") ?? "";
    const [status, setStatus] = useState(token ? "loading" : "error");
    const [message, setMessage] = useState(
        token ? "" : "El enlace de verificación no incluye un token válido."
    );
    const alreadyRun = useRef(false);

    useEffect(() => {
        if (!token || alreadyRun.current) return;
        alreadyRun.current = true;

        verify(token)
            .then((res) => {
                setStatus("ok");
                setMessage(res.message || "Tu cuenta fue verificada correctamente.");
            })
            .catch((err) => {
                setStatus("error");
                setMessage(
                    err.message ||
                        "El enlace de verificación no es válido o ya fue utilizado."
                );
            });
    }, [token]);

    if (status === "loading") {
        return <PageLoader message="Verificando tu cuenta…" />;
    }

    return (
        <div className="auth-wrap">
            <div className="auth-card" style={{ maxWidth: 480 }}>
                <div className="auth-card__head">
                    <Isotype negative />
                    <Wordmark negative />
                </div>

                <div className="auth-card__body">
                    <h1 className="font-title">Verificación de cuenta</h1>

                    {status === "ok" ? (
                        <>
                            <Alert type="success">{message}</Alert>
                            <Link
                                to="/login?verificado"
                                className="btn btn-primary btn-block mt-16"
                            >
                                Iniciar sesión
                            </Link>
                        </>
                    ) : (
                        <>
                            <Alert type="error">{message}</Alert>
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
