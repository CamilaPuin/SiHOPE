package edu.uptc.swii.sihope.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TimeBlock(
        @JsonProperty("diaSemana") int dayOfWeek,
        @JsonProperty("horaInicio") String startTime,
        @JsonProperty("horaFin") String endTime) {

    public int diaSemana() {
        return dayOfWeek;
    }

    public String horaInicio() {
        return startTime;
    }

    public String horaFin() {
        return endTime;
    }
}
