/**
 * Campo de formulario (label + input/select + mensaje de error), reproduciendo
 * la estructura `.field` de sihope.css. Cuando `error` tiene texto, aplica la
 * clase `.has-error` y muestra el mensaje.
 *
 * Renderiza un <input> por defecto; para un <select> u otro control, pasa el
 * control como `children` (se ignora el resto de props de input).
 */
export default function Field({
    label,
    id,
    error,
    hint,
    children,
    className = "",
    ...inputProps
}) {
    return (
        <div className={`field ${error ? "has-error" : ""} ${className}`.trim()}>
            {label && (
                <label htmlFor={id}>
                    {label} {hint && <span className="hint">{hint}</span>}
                </label>
            )}
            {children ?? <input id={id} {...inputProps} />}
            {error && <div className="field__error">{error}</div>}
        </div>
    );
}
