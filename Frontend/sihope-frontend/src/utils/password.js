export const PASSWORD_RULES = [
    { key: "len", label: "Mínimo 8 caracteres", test: (v) => v.length >= 8 },
    { key: "upper", label: "Una letra mayúscula", test: (v) => /[A-Z]/.test(v) },
    { key: "lower", label: "Una letra minúscula", test: (v) => /[a-z]/.test(v) },
    { key: "digit", label: "Un número", test: (v) => /\d/.test(v) },
    {
        key: "special",
        label: "Un carácter especial (!@#$…)",
        test: (v) => /[^A-Za-z0-9]/.test(v)
    }
];

export function isPasswordValid(value) {
    return PASSWORD_RULES.every((r) => r.test(value));
}

export const UPTC_EMAIL = /@uptc\.edu\.co$/i;
