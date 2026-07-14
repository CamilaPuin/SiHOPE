package edu.uptc.swii.sihope.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import edu.uptc.swii.sihope.domain.Cita;

public record CitaResponse(
        Integer id,
        @JsonProperty("estudianteId") Integer studentId,
        @JsonProperty("estudiante") String studentName,
        @JsonProperty("monitorId") Integer monitorId,
        @JsonProperty("monitor") String monitorName,
        @JsonProperty("asignaturaId") Integer subjectId,
        @JsonProperty("asignatura") String subjectName,
        @JsonProperty("tema") String topic,
        @JsonProperty("fecha") String date,
        @JsonProperty("horaInicio") String startTime,
        @JsonProperty("horaFin") String endTime,
        @JsonProperty("estado") String status,
        @JsonProperty("motivoCancelacion") String cancellationReason,
        @JsonProperty("esMonitor") boolean viewerIsMonitor) {

    public static CitaResponse from(Cita c, Integer viewerId) {
        return new CitaResponse(
                c.getId(),
                c.getStudent() != null ? c.getStudent().getId() : null,
                fullName(c.getStudent()),
                c.getMonitor() != null ? c.getMonitor().getId() : null,
                fullName(c.getMonitor()),
                c.getSubject() != null ? c.getSubject().getId() : null,
                c.getSubject() != null ? c.getSubject().getName() : null,
                c.getTopic(),
                c.getDate() != null ? c.getDate().toString() : null,
                c.getStartTime() != null ? c.getStartTime().toString() : null,
                c.getEndTime() != null ? c.getEndTime().toString() : null,
                c.getStatus(),
                c.getCancellationReason(),
                viewerId != null && c.getMonitor() != null && viewerId.equals(c.getMonitor().getId()));
    }

    private static String fullName(edu.uptc.swii.sihope.domain.User u) {
        if (u == null) {
            return null;
        }
        return ((u.getFirstName() == null ? "" : u.getFirstName()) + " "
                + (u.getLastName() == null ? "" : u.getLastName())).trim();
    }
}
