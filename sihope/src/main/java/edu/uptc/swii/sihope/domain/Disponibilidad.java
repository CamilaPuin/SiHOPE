package edu.uptc.swii.sihope.domain;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Bloque de disponibilidad horaria semanal de un monitor (HU_006). Cada fila
 * representa una franja recurrente: un día de la semana y un rango de horas en el
 * que el monitor puede atender monitorías.
 */
@Entity
@Table(name = "disponibilidad")
public class Disponibilidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "monitor_id")
    private User monitor;

    /** Día de la semana: 1 = Lunes … 7 = Domingo. */
    @Column(name = "dia_semana")
    private int diaSemana;

    @Column(name = "hora_inicio")
    private LocalTime horaInicio;

    @Column(name = "hora_fin")
    private LocalTime horaFin;

    public Disponibilidad() {
    }

    public Disponibilidad(User monitor, int diaSemana, LocalTime horaInicio, LocalTime horaFin) {
        this.monitor = monitor;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
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

    public int getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(int diaSemana) {
        this.diaSemana = diaSemana;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
}
