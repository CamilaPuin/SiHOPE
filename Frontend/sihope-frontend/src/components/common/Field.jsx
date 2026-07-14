
export default function Field({
    label,
    id,
    error,
    hint,
    children,
    className = "",
    ...inputProps
}) {
    const { className: inputClass, ...restProps } = inputProps || {};

    return (
        <div className={`field ${error ? "has-error" : ""} ${className}`.trim()}>
            {label && (
                <label htmlFor={id}>
                    {label} {hint && <span className="hint">{hint}</span>}
                </label>
            )}
            {children ?? (
                <input id={id} className={`input ${inputClass || ""}`.trim()} {...restProps} />
            )}
            {error && <div className="field__error">{error}</div>}
        </div>
    );
}
