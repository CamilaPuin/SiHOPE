package edu.uptc.swii.sihope.dto.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CitasReportResponse(
        @JsonProperty("desde") String from,
        @JsonProperty("hasta") String to,
        @JsonProperty("total") long total,
        @JsonProperty("porMonitor") List<ReportRow> byMonitor,
        @JsonProperty("porTema") List<ReportRow> bySubject,
        @JsonProperty("citas") List<CitaDetailRow> details,
        @JsonProperty("mensaje") String message) {

    public record ReportRow(
            @JsonProperty("nombre") String name,
            @JsonProperty("total") long total) {
    }

    public record CitaDetailRow(
            @JsonProperty("estudiante") String student,
            @JsonProperty("asignatura") String subject,
            @JsonProperty("fecha") String date,
            @JsonProperty("horaInicio") String startTime,
            @JsonProperty("horaFin") String endTime) {
    }
}
