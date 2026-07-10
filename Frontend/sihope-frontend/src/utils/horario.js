/**
 * Utilidades para presentar la disponibilidad horaria (HU_006).
 * Los bloques llegan del backend como { diaSemana (1-7), horaInicio, horaFin }
 * con las horas en formato "HH:mm" (o "HH:mm:ss").
 */

const DIAS = ["", "Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"];

export const nombreDia = (n) => DIAS[n] ?? "—";

/** Normaliza una hora a "HH:mm" (descarta segundos si vienen). */
export const hhmm = (hora) => (hora ? String(hora).slice(0, 5) : "");

/** Minutos que dura un bloque. */
function minutosBloque(b) {
    const [hi, mi] = hhmm(b.horaInicio).split(":").map(Number);
    const [hf, mf] = hhmm(b.horaFin).split(":").map(Number);
    if ([hi, mi, hf, mf].some((n) => Number.isNaN(n))) return 0;
    return hf * 60 + mf - (hi * 60 + mi);
}

/** Total de horas configuradas, redondeado a un decimal (p. ej. "6.5 h"). */
export function totalHoras(bloques) {
    const min = (bloques ?? []).reduce((acc, b) => acc + Math.max(0, minutosBloque(b)), 0);
    const horas = min / 60;
    return `${Number.isInteger(horas) ? horas : horas.toFixed(1)} h`;
}

/** Ordena los bloques por día y hora de inicio. */
export function ordenarBloques(bloques) {
    return [...(bloques ?? [])].sort(
        (a, b) => a.diaSemana - b.diaSemana || hhmm(a.horaInicio).localeCompare(hhmm(b.horaInicio))
    );
}

/** Texto corto de un bloque: "10:00 – 12:00". */
export const rangoBloque = (b) => `${hhmm(b.horaInicio)} – ${hhmm(b.horaFin)}`;
