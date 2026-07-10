
export default function Alert({ tipo = "info", children, className = "" }) {
    if (!children) return null;
    return (
        <div className={`alert alert-${tipo} ${className}`.trim()} role="alert">
            {children}
        </div>
    );
}
