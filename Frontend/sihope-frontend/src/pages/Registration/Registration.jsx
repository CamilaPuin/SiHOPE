import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Isotype, { Wordmark } from "../../components/layout/Isotype";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import PasswordRequirements from "../../components/common/PasswordRequirements";
import { register } from "../../services/registrationService";
import { UPTC_EMAIL, isPasswordValid } from "../../utils/password";

const INITIAL = {
    nombres: "",
    apellidos: "",
    codigo: "",
    correo: "",
    password: "",
    password2: ""
};

export default function Registration() {
    const navigate = useNavigate();
    const [form, setForm] = useState(INITIAL);
    const [errors, setErrors] = useState({});
    const [generalError, setGeneralError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const update = (field) => (e) =>
        setForm((prev) => ({ ...prev, [field]: e.target.value }));

    const validate = () => {
        const errs = {};
        if (!form.nombres.trim()) errs.nombres = "Ingresa tus nombres.";
        if (!form.apellidos.trim()) errs.apellidos = "Ingresa tus apellidos.";
        if (!form.codigo.trim()) errs.codigo = "El código estudiantil es obligatorio.";
        if (!UPTC_EMAIL.test(form.correo.trim()))
            errs.correo = "Debes usar un correo institucional de la UPTC (@uptc.edu.co).";
        if (!isPasswordValid(form.password))
            errs.password = "La contraseña no cumple los requisitos de seguridad.";
        if (form.password !== form.password2 || !form.password2)
            errs.password2 = "Las contraseñas no coinciden.";
        return errs;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setGeneralError("");
        const clientErrors = validate();
        if (Object.keys(clientErrors).length > 0) {
            setErrors(clientErrors);
            return;
        }
        setErrors({});
        setSubmitting(true);
        try {
            await register(form);
            navigate("/login?registered");
        } catch (err) {
            if (err.data && typeof err.data === "object") {
                setErrors(err.data);
            } else {
                setGeneralError(err.message);
            }
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="auth-wrap">
            <div className="auth-card">
                <div className="auth-card__head">
                    <Isotype negative />
                    <Wordmark negative />
                </div>

                <div className="auth-card__body">
                    <h1 className="font-title">Crea tu cuenta de estudiante</h1>
                    <p className="subtitle">
                        Regístrate con tu correo institucional de la UPTC. Deberás verificar
                        tu correo antes de iniciar sesión por primera vez.
                    </p>

                    <Alert type="error">{generalError}</Alert>
                    {Object.keys(errors).length > 0 && (
                        <Alert type="error">
                            Revisa los campos marcados y vuelve a intentarlo.
                        </Alert>
                    )}

                    <form onSubmit={handleSubmit} noValidate>
                        <div className="form-grid">
                            <Field
                                label="Nombres"
                                id="nombres"
                                value={form.nombres}
                                onChange={update("nombres")}
                                placeholder="Pedrito José"
                                error={errors.nombres}
                            />
                            <Field
                                label="Apellidos"
                                id="apellidos"
                                value={form.apellidos}
                                onChange={update("apellidos")}
                                placeholder="Ojeda Puin"
                                error={errors.apellidos}
                            />
                            <Field
                                label="Código estudiantil"
                                id="codigo"
                                value={form.codigo}
                                onChange={update("codigo")}
                                inputMode="numeric"
                                placeholder="202312345"
                                error={errors.codigo}
                            />
                            <Field
                                className="col-2"
                                label="Correo institucional"
                                hint="(@uptc.edu.co)"
                                id="correo"
                                type="email"
                                value={form.correo}
                                onChange={update("correo")}
                                placeholder="nombre.apellido@uptc.edu.co"
                                error={errors.correo}
                            />
                            <Field
                                label="Contraseña"
                                id="password"
                                type="password"
                                value={form.password}
                                onChange={update("password")}
                                error={errors.password}
                            />
                            <Field
                                label="Confirmar contraseña"
                                id="password2"
                                type="password"
                                value={form.password2}
                                onChange={update("password2")}
                                error={errors.password2}
                            />
                        </div>

                        <PasswordRequirements value={form.password} />

                        <button
                            type="submit"
                            className="btn btn-primary btn-block mt-16"
                            disabled={submitting}
                        >
                            {submitting ? <Spinner /> : "Crear cuenta"}
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
