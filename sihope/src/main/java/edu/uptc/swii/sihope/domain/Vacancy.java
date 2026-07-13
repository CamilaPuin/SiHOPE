package edu.uptc.swii.sihope.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
@Table(name = "convocatoria")
public class Vacancy {

    public static final String ABIERTA = "ABIERTA";
    public static final String CERRADA = "CERRADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titulo")
    @JsonProperty("titulo")
    private String title;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    @JsonProperty("descripcion")
    private String description;

    @Column(name = "requisitos", columnDefinition = "TEXT")
    @JsonProperty("requisitos")
    private String requirements;

    @Column(name = "materia")
    @JsonProperty("materia")
    private String subject;

    @Column(name = "plazas")
    @JsonProperty("plazas")
    private int slots;

    @Column(name = "fecha_limite")
    @JsonProperty("fechaLimite")
    private LocalDate deadline;

    @Column(name = "estado")
    @JsonProperty("estado")
    private String status;

    @Column(name = "fecha_creacion")
    @JsonProperty("fechaCreacion")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "coordinador_id")
    @JsonProperty("coordinador")
    private User coordinator;

    public Vacancy() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public int getSlots() {
        return slots;
    }

    public void setSlots(int slots) {
        this.slots = slots;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getCoordinator() {
        return coordinator;
    }

    public void setCoordinator(User coordinator) {
        this.coordinator = coordinator;
    }

    public String getTitulo() {
        return getTitle();
    }

    public void setTitulo(String titulo) {
        setTitle(titulo);
    }

    public String getDescripcion() {
        return getDescription();
    }

    public void setDescripcion(String descripcion) {
        setDescription(descripcion);
    }

    public String getRequisitos() {
        return getRequirements();
    }

    public void setRequisitos(String requisitos) {
        setRequirements(requisitos);
    }

    public String getMateria() {
        return getSubject();
    }

    public void setMateria(String materia) {
        setSubject(materia);
    }

    public int getPlazas() {
        return getSlots();
    }

    public void setPlazas(int plazas) {
        setSlots(plazas);
    }

    public LocalDate getFechaLimite() {
        return getDeadline();
    }

    public void setFechaLimite(LocalDate fechaLimite) {
        setDeadline(fechaLimite);
    }

    public String getEstado() {
        return getStatus();
    }

    public void setEstado(String estado) {
        setStatus(estado);
    }

    public LocalDateTime getFechaCreacion() {
        return getCreatedAt();
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        setCreatedAt(fechaCreacion);
    }

    public User getCoordinador() {
        return getCoordinator();
    }

    public void setCoordinador(User coordinador) {
        setCoordinator(coordinador);
    }
}
