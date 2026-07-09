/** Indicador de carga circular. `claro` para fondos oscuros; `grande` para bloques. */
export default function Spinner({ claro = false, grande = false }) {
    const clases = [
        "spinner",
        claro ? "spinner--light" : "",
        grande ? "spinner--lg" : ""
    ]
        .filter(Boolean)
        .join(" ");
    return <span className={clases} role="status" aria-label="Cargando" />;
}

/** Cargador a página completa, usado mientras se resuelve la sesión inicial. */
export function PageLoader({ mensaje = "Cargando…" }) {
    return (
        <div className="page-loader">
            <Spinner grande />
            <span>{mensaje}</span>
        </div>
    );
}
