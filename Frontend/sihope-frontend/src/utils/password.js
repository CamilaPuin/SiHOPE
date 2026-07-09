/**
 * Reglas de contraseña, equivalentes a las validadas en el backend
 * (mín. 8, mayúscula, minúscula, dígito y carácter especial).
 */
export const REGLAS_PASSWORD = [
    { clave: "len", etiqueta: "Mínimo 8 caracteres", test: (v) => v.length >= 8 },
    { clave: "upper", etiqueta: "Una letra mayúscula", test: (v) => /[A-Z]/.test(v) },
    { clave: "lower", etiqueta: "Una letra minúscula", test: (v) => /[a-z]/.test(v) },
    { clave: "digit", etiqueta: "Un número", test: (v) => /\d/.test(v) },
    {
        clave: "special",
        etiqueta: "Un carácter especial (!@#$…)",
        test: (v) => /[^A-Za-z0-9]/.test(v)
    }
];

/** true si la contraseña cumple todas las reglas. */
export function passwordValida(valor) {
    return REGLAS_PASSWORD.every((r) => r.test(valor));
}

/** Correo institucional de la UPTC. */
export const CORREO_UPTC = /@uptc\.edu\.co$/i;
