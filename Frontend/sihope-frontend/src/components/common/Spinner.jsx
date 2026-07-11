export default function Spinner({ light = false, large = false }) {
    const classes = [
        "spinner",
        light ? "spinner--light" : "",
        large ? "spinner--lg" : ""
    ]
        .filter(Boolean)
        .join(" ");
    return <span className={classes} role="status" aria-label="Cargando" />;
}

export function PageLoader({ message = "Cargando…" }) {
    return (
        <div className="page-loader">
            <Spinner large />
            <span>{message}</span>
        </div>
    );
}
