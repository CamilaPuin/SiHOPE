package edu.uptc.swii.sihope.domain;

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
@Table(name = "postulacion")
public class Application {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String APROBADA = "APROBADA";
    public static final String RECHAZADA = "RECHAZADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "convocatoria_id")
    @JsonProperty("convocatoria")
    private Vacancy vacancy;

    @ManyToOne
    @JoinColumn(name = "aspirante_id")
    @JsonProperty("aspirante")
    private User applicant;

    @Column(name = "estado")
    @JsonProperty("estado")
    private String state;

    @Column(name = "datos_json", columnDefinition = "TEXT")
    @JsonProperty("datosJson")
    private String dataJson;

    @Column(name = "fecha_postulacion")
    @JsonProperty("fechaPostulacion")
    private LocalDateTime appliedAt;

    public Application() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Vacancy getVacancy() {
        return vacancy;
    }

    public void setVacancy(Vacancy vacancy) {
        this.vacancy = vacancy;
    }

    public User getApplicant() {
        return applicant;
    }

    public void setApplicant(User applicant) {
        this.applicant = applicant;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }

    // Spanish aliases for compatibility
    public Vacancy getConvocatoria() {
        return getVacancy();
    }

    public void setConvocatoria(Vacancy convocatoria) {
        setVacancy(convocatoria);
    }

    public User getAspirante() {
        return getApplicant();
    }

    public void setAspirante(User aspirante) {
        setApplicant(aspirante);
    }

    public String getEstado() {
        return getState();
    }

    public void setEstado(String estado) {
        setState(estado);
    }

    public String getDatosJson() {
        return getDataJson();
    }

    public void setDatosJson(String datosJson) {
        setDataJson(datosJson);
    }

    public LocalDateTime getFechaPostulacion() {
        return getAppliedAt();
    }

    public void setFechaPostulacion(LocalDateTime fechaPostulacion) {
        setAppliedAt(fechaPostulacion);
    }
}
