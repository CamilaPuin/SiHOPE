package edu.uptc.swii.sihope.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "cita")
public class Cita {

    public static final String RESERVADA = "RESERVADA";
    public static final String CONFIRMADA = "CONFIRMADA";
    public static final String CANCELADA = "CANCELADA";
    public static final String ATENDIDA = "ATENDIDA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "monitor_id")
    private User monitor;

    @ManyToOne
    @JoinColumn(name = "asignatura_id")
    private Asignatura subject;

    @Column(name = "fecha")
    private LocalDate date;

    @Column(name = "hora_inicio")
    private LocalTime startTime;

    @Column(name = "hora_fin")
    private LocalTime endTime;

    @Column(name = "estado")
    private String status;

    @Column(name = "motivo_cancelacion")
    private String cancellationReason;

    @Column(name = "recordatorio_enviado", nullable = false)
    private boolean reminderSent = false;

    @Column(name = "fecha_creacion")
    private LocalDateTime createdAt;

    @Column(name = "slot_key")
    private String slotKey;

    public Cita() {
    }

    public static String buildSlotKey(Integer monitorId, LocalDate date, LocalTime startTime) {
        return monitorId + "|" + date + "|" + startTime;
    }

    public LocalDateTime startsAt() {
        return LocalDateTime.of(date, startTime);
    }

    public LocalDateTime endsAt() {
        return LocalDateTime.of(date, endTime);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public User getMonitor() {
        return monitor;
    }

    public void setMonitor(User monitor) {
        this.monitor = monitor;
    }

    public Asignatura getSubject() {
        return subject;
    }

    public void setSubject(Asignatura subject) {
        this.subject = subject;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getSlotKey() {
        return slotKey;
    }

    public void setSlotKey(String slotKey) {
        this.slotKey = slotKey;
    }
}
