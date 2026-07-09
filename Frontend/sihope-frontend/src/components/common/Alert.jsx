/**
 * Mensaje contextual (éxito / error / info). Reproduce las clases `.alert-*`
 * de sihope.css. No renderiza nada si no hay `children`.
 *
 * @param {"success"|"error"|"info"} tipo
 */
export default function Alert({ tipo = "info", children, className = "" }) {
    if (!children) return null;
    return (
        <div className={`alert alert-${tipo} ${className}`.trim()} role="alert">
            {children}
        </div>
    );
}
