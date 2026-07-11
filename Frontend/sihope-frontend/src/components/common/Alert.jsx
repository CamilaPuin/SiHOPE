export default function Alert({ type = "info", children, className = "" }) {
    if (!children) return null;
    return (
        <div className={`alert alert-${type} ${className}`.trim()} role="alert">
            {children}
        </div>
    );
}
