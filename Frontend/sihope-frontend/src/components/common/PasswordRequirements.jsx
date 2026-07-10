import { REGLAS_PASSWORD } from "../../utils/password";


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
