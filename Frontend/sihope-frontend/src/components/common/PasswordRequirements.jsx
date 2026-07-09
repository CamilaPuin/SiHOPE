import { REGLAS_PASSWORD } from "../../utils/password";

/**
 * Lista de requisitos de contraseña que se resaltan en vivo según el valor.
 * Reproduce el bloque `.pwd-reqs` de los mockups.
 */
export default function PasswordRequirements({ valor = "" }) {
    return (
        <ul className="pwd-reqs">
            {REGLAS_PASSWORD.map((regla) => (
                <li key={regla.clave} className={regla.test(valor) ? "ok" : ""}>
                    {regla.etiqueta}
                </li>
            ))}
        </ul>
    );
}
