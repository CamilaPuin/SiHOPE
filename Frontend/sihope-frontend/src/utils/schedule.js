const DAYS = ["", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado"];

export const dayName = (n) => DAYS[n] ?? "—";

export const hhmm = (hora) => (hora ? String(hora).slice(0, 5) : "");

function blockMinutes(b) {
    const [hi, mi] = hhmm(b.horaInicio).split(":").map(Number);
    const [hf, mf] = hhmm(b.horaFin).split(":").map(Number);
    if ([hi, mi, hf, mf].some((n) => Number.isNaN(n))) return 0;
    return hf * 60 + mf - (hi * 60 + mi);
}

export function totalHours(blocks) {
    const min = (blocks ?? []).reduce((acc, b) => acc + Math.max(0, blockMinutes(b)), 0);
    const hours = min / 60;
    return `${Number.isInteger(hours) ? hours : hours.toFixed(1)} h`;
}

export function sortBlocks(blocks) {
    return [...(blocks ?? [])].sort(
        (a, b) => a.diaSemana - b.diaSemana || hhmm(a.horaInicio).localeCompare(hhmm(b.horaInicio))
    );
}

export const blockRange = (b) => `${hhmm(b.horaInicio)} – ${hhmm(b.horaFin)}`;
