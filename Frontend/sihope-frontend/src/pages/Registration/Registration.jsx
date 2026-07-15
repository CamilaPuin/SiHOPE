import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Isotype from "../../components/layout/Isotype";
import logo from "../../images/logo-sihope.jpeg";
import Field from "../../components/common/Field";
import Alert from "../../components/common/Alert";
import Spinner from "../../components/common/Spinner";
import PasswordRequirements from "../../components/common/PasswordRequirements";
import { register, listRegistrationCareers } from "../../services/registrationService";
import { UPTC_EMAIL, isPasswordValid } from "../../utils/password";

const INITIAL = {
    nombres: "",
    apellidos: "",
    codigo: "",
    correo: "",
    carreraId: "",
    password: "",
    password2: ""
};

const NAME_PATTERN = /^\p{L}+(?:\s\p{L}+)*$/u;
const CODE_PATTERN = /^[A-Za-z0-9]{1,15}$/;

export default function Registration() {
    const navigate = useNavigate();
    const [form, setForm] = useState(INITIAL);
    const [errors, setErrors] = useState({});
    const [generalError, setGeneralError] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [careers, setCareers] = useState([]);

    useEffect(() => {
        listRegistrationCareers()
            .then((res) => setCareers(res.data ?? []))
            .catch(() => setCareers([]));
    }, []);

    const update = (field) => (e) =>
        setForm((prev) => ({ ...prev, [field]: e.target.value }));

    const validate = () => {
        const errs = {};
        const nombres = form.nombres.trim();
        const apellidos = form.apellidos.trim();
        const codigo = form.codigo.trim();
        if (!nombres) errs.nombres = "Ingresa tus nombres.";
        else if (nombres.length > 50 || !NAME_PATTERN.test(nombres))
            errs.nombres = "Los nombres solo pueden contener letras y espacios (máx. 50 caracteres).";
        if (!apellidos) errs.apellidos = "Ingresa tus apellidos.";
        else if (apellidos.length > 50 || !NAME_PATTERN.test(apellidos))
            errs.apellidos = "Los apellidos solo pueden contener letras y espacios (máx. 50 caracteres).";
        if (!codigo) errs.codigo = "El código estudiantil es obligatorio.";
        else if (!CODE_PATTERN.test(codigo))
            errs.codigo = "El código debe ser alfanumérico, sin espacios ni caracteres especiales (máx. 15 caracteres).";
        if (!UPTC_EMAIL.test(form.correo.trim()))
            errs.correo = "Ingresa un correo electrónico válido.";
        if (!form.carreraId) errs.carrera = "Selecciona tu carrera.";
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
            await register({ ...form, carreraId: Number(form.carreraId) });
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
                    <Isotype src={logo} negative />
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
                                className="col-2"
                                label="Carrera"
                                id="carrera"
                                error={errors.carrera}
                            >
                                <select
                                    id="carrera"
                                    className="input"
                                    value={form.carreraId}
                                    onChange={update("carreraId")}
                                >
                                    <option value="">Selecciona tu carrera…</option>
                                    {careers.map((c) => (
                                        <option key={c.id} value={c.id}>
                                            {c.nombre}
                                        </option>
                                    ))}
                                </select>
                            </Field>
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
