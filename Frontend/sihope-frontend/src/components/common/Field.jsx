import { useState } from "react";

export default function Field({
    label,
    id,
    error,
    hint,
    children,
    className = "",
    ...inputProps
}) {
    const { className: inputClass, type, ...restProps } = inputProps || {};
    const [showPassword, setShowPassword] = useState(false);

    const isPassword = type === "password";
    const inputType = isPassword && showPassword ? "text" : type;

    return (
        <div className={`field ${error ? "has-error" : ""} ${className}`.trim()}>
            {label && (
                <label htmlFor={id}>
                    {label} {hint && <span className="hint">{hint}</span>}
                </label>
            )}
            {children ?? (
                isPassword ? (
                    <div className="field__password">
                        <input
                            id={id}
                            type={inputType}
                            className={`input ${inputClass || ""}`.trim()}
                            {...restProps}
                        />
                        <button
                            type="button"
                            className="field__toggle"
                            onClick={() => setShowPassword((v) => !v)}
                            aria-label={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                            aria-pressed={showPassword}
                            title={showPassword ? "Ocultar contraseña" : "Mostrar contraseña"}
                        >
                            {showPassword ? (
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                    <path d="M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                                    <path d="M1 1l22 22" />
                                    <path d="M6.61 6.61A18.5 18.5 0 0 0 1 12s4 8 11 8a9.12 9.12 0 0 0 5.39-1.61" />
                                </svg>
                            ) : (
                                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
                                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                                    <circle cx="12" cy="12" r="3" />
                                </svg>
                            )}
                        </button>
                    </div>
                ) : (
                    <input id={id} type={type} className={`input ${inputClass || ""}`.trim()} {...restProps} />
                )
            )}
            {error && <div className="field__error">{error}</div>}
        </div>
    );
}
