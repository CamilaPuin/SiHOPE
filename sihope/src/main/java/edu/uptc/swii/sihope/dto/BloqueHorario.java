package edu.uptc.swii.sihope.dto;

/**
 * Franja horaria de disponibilidad (HU_006), usada tanto en la petición como en la
 * respuesta. Las horas viajan como texto "HH:mm" para interoperar sin ambigüedad
 * con FullCalendar en el frontend.
 *
 * @param diaSemana  1 = Lunes … 7 = Domingo
 * @param horaInicio hora de inicio en formato "HH:mm"
 * @param horaFin    hora de fin en formato "HH:mm"
 */
public record BloqueHorario(int diaSemana, String horaInicio, String horaFin) {
}
