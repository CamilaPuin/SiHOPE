import { PASSWORD_RULES } from "../../utils/password";

export default function PasswordRequirements({ value = "" }) {
    return (
        <ul className="pwd-reqs">
            {PASSWORD_RULES.map((rule) => (
                <li key={rule.key} className={rule.test(value) ? "ok" : ""}>
                    {rule.label}
                </li>
            ))}
        </ul>
    );
}
