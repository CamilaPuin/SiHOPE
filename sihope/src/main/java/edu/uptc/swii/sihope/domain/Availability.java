package edu.uptc.swii.sihope.domain;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "disponibilidad")
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "monitor_id")
    @JsonProperty("monitor")
    private User monitor;

    @Column(name = "dia_semana")
    @JsonProperty("diaSemana")
    private int dayOfWeek;

    @Column(name = "hora_inicio")
    @JsonProperty("horaInicio")
    private LocalTime startTime;

    @Column(name = "hora_fin")
    @JsonProperty("horaFin")
    private LocalTime endTime;

    public Availability() {
    }

    public Availability(User monitor, int dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.monitor = monitor;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getMonitor() {
        return monitor;
    }

    public void setMonitor(User monitor) {
        this.monitor = monitor;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    // Spanish aliases for compatibility
    public int getDiaSemana() {
        return getDayOfWeek();
    }

    public void setDiaSemana(int diaSemana) {
        setDayOfWeek(diaSemana);
    }

    public LocalTime getHoraInicio() {
        return getStartTime();
    }

    public void setHoraInicio(LocalTime horaInicio) {
        setStartTime(horaInicio);
    }

    public LocalTime getHoraFin() {
        return getEndTime();
    }

    public void setHoraFin(LocalTime horaFin) {
        setEndTime(horaFin);
    }
}
