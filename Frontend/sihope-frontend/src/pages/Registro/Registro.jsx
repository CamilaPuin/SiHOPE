import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Isotipo, { Wordmark } from "../../components/layout/Isotipo";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import PasswordRequirements from "../../components/common/PasswordRequirements";
import { registrar } from "../../services/registroService";
import { CORREO_UPTC, passwordValida } from "../../utils/password";

const INICIAL = {
    nombres: "",
    apellidos: "",
    codigo: "",
    correo: "",
    password: "",
    password2: ""
};

export default function Registro() {
    const navigate = useNavigate();
    const [form, setForm] = useState(INICIAL);
    const [errores, setErrores] = useState({});
    const [errorGeneral, setErrorGeneral] = useState("");
    const [enviando, setEnviando] = useState(false);

    const actualizar = (campo) => (e) =>
        setForm((prev) => ({ ...prev, [campo]: e.target.value }));

    /** Validación en cliente antes de llamar al backend. */
    const validar = () => {
        const errs = {};
        if (!form.nombres.trim()) errs.nombres = "Ingresa tus nombres.";
        if (!form.apellidos.trim()) errs.apellidos = "Ingresa tus apellidos.";
        if (!form.codigo.trim()) errs.codigo = "El código estudiantil es obligatorio.";
        if (!CORREO_UPTC.test(form.correo.trim()))
            errs.correo = "Debes usar un correo institucional de la UPTC (@uptc.edu.co).";
        if (!passwordValida(form.password))
            errs.password = "La contraseña no cumple los requisitos de seguridad.";
        if (form.password !== form.password2 || !form.password2)
            errs.password2 = "Las contraseñas no coinciden.";
        return errs;
    };

    const manejarSubmit = async (e) => {
        e.preventDefault();
        setErrorGeneral("");
        const errsCliente = validar();
        if (Object.keys(errsCliente).length > 0) {
            setErrores(errsCliente);
            return;
        }
        setErrores({});
        setEnviando(true);
        try {
            await registrar(form);
            navigate("/login?registrado");
        } catch (err) {
            // El backend devuelve un mapa { campo: mensaje } en err.data.
            if (err.data && typeof err.data === "object") {
                setErrores(err.data);
            } else {
                setErrorGeneral(err.message);
            }
        } finally {
            setEnviando(false);
        }
    };

    return (
        <div className="auth-wrap">
            <div className="auth-card">
                <div className="auth-card__head">
                    <Isotipo negativo />
                    <Wordmark negativo />
                </div>

                <div className="auth-card__body">
                    <h1 className="font-title">Crea tu cuenta de estudiante</h1>
                    <p className="subtitle">
                        Regístrate con tu correo institucional de la UPTC. Deberás verificar
                        tu correo antes de iniciar sesión por primera vez.
                    </p>

                    <Alert tipo="error">{errorGeneral}</Alert>
                    {Object.keys(errores).length > 0 && (
                        <Alert tipo="error">
                            Revisa los campos marcados y vuelve a intentarlo.
                        </Alert>
                    )}

                    <form onSubmit={manejarSubmit} noValidate>
                        <div className="form-grid">
                            <Field
                                className="col-2"
                                label="Nombres"
                                id="nombres"
                                value={form.nombres}
                                onChange={actualizar("nombres")}
                                placeholder="Juan Manuel"
                                error={errores.nombres}
                            />
                            <Field
                                className="col-2"
                                label="Apellidos"
                                id="apellidos"
                                value={form.apellidos}
                                onChange={actualizar("apellidos")}
                                placeholder="Ojeda Sánchez"
                                error={errores.apellidos}
                            />
                            <Field
                                className="col-2"
                                label="Código estudiantil"
                                id="codigo"
                                value={form.codigo}
                                onChange={actualizar("codigo")}
                                inputMode="numeric"
                                placeholder="202312345"
                                error={errores.codigo}
                            />
                            <Field
                                className="col-2"
                                label="Correo institucional"
                                hint="(@uptc.edu.co)"
                                id="correo"
                                type="email"
                                value={form.correo}
                                onChange={actualizar("correo")}
                                placeholder="nombre.apellido@uptc.edu.co"
                                error={errores.correo}
                            />
                            <Field
                                label="Contraseña"
                                id="password"
                                type="password"
                                value={form.password}
                                onChange={actualizar("password")}
                                error={errores.password}
                            />
                            <Field
                                label="Confirmar contraseña"
                                id="password2"
                                type="password"
                                value={form.password2}
                                onChange={actualizar("password2")}
                                error={errores.password2}
                            />
                        </div>

                        <PasswordRequirements valor={form.password} />

                        <button
                            type="submit"
                            className="btn btn-primary btn-block mt-16"
                            disabled={enviando}
                        >
                            {enviando ? <Spinner /> : "Crear cuenta"}
                        </button>
                    </form>

                    <p className="auth-foot">
                        ¿Ya tienes cuenta?{" "}
                        <Link className="link-strong" to="/login">
                            Inicia sesión
                        </Link>
                    </p>
                </div>
            </div>
        </div>
    );
}
