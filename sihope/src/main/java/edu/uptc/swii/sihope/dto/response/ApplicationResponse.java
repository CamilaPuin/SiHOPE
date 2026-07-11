package edu.uptc.swii.sihope.dto.response;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApplicationResponse(
        Integer id,
        @JsonProperty("convocatoriaId") Integer vacancyId,
        @JsonProperty("convocatoriaTitulo") String vacancyTitle,
        @JsonProperty("aspiranteId") Integer applicantId,
        @JsonProperty("aspiranteNombre") String applicantName,
        @JsonProperty("aspiranteCorreo") String applicantEmail,
        @JsonProperty("estado") String status,
        @JsonProperty("datos") Map<String, String> data,
        @JsonProperty("fechaPostulacion") String applicationDate) {

    public Integer convocatoriaId() {
        return vacancyId;
    }

    public String convocatoriaTitulo() {
        return vacancyTitle;
    }

    public Integer aspiranteId() {
        return applicantId;
    }

    public String aspiranteNombre() {
        return applicantName;
    }

    public String aspiranteCorreo() {
        return applicantEmail;
    }

    public String estado() {
        return status;
    }

    public Map<String, String> datos() {
        return data;
    }

    public String fechaPostulacion() {
        return applicationDate;
    }
}
