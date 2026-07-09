/** Pie de página institucional. Portado del fragmento `footer` de layout.html. */
export default function Footer() {
    return (
        <footer className="footer">
            <span>
                SiHope · Escuela de Ingeniería de Sistemas y Computación, UPTC
            </span>
            <span>
                <a href="#">Soporte</a> · <a href="#">Términos</a> ·{" "}
                <a href="#">Contacto</a> ·{" "}
                <span className="muted">2026 SiHope. Todos los derechos reservados.</span>
            </span>
        </footer>
    );
}
