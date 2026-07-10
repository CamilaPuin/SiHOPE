package edu.uptc.swii.sihope.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Postulación de un estudiante aspirante a una convocatoria (HU_005 / HU_009).
 * Los campos capturados en el formulario se guardan como JSON en {@code datosJson}
 * para que el formulario sea parametrizable sin cambios de esquema.
 */
@Entity
@Table(name = "postulacion")
public class Postulacion {

    public static final String PENDIENTE = "PENDIENTE";
    public static final String APROBADA = "APROBADA";
    public static final String RECHAZADA = "RECHAZADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "convocatoria_id")
    private Convocatoria convocatoria;

    @ManyToOne
    @JoinColumn(name = "aspirante_id")
    private User aspirante;

    private String estado;

    @Column(name = "datos_json", columnDefinition = "TEXT")
    private String datosJson;

    @Column(name = "fecha_postulacion")
    private LocalDateTime fechaPostulacion;

    public Postulacion() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Convocatoria getConvocatoria() {
        return convocatoria;
    }

    public void setConvocatoria(Convocatoria convocatoria) {
        this.convocatoria = convocatoria;
    }

    public User getAspirante() {
        return aspirante;
    }

    public void setAspirante(User aspirante) {
        this.aspirante = aspirante;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDatosJson() {
        return datosJson;
    }

    public void setDatosJson(String datosJson) {
        this.datosJson = datosJson;
    }

    public LocalDateTime getFechaPostulacion() {
        return fechaPostulacion;
    }

    public void setFechaPostulacion(LocalDateTime fechaPostulacion) {
        this.fechaPostulacion = fechaPostulacion;
    }
}
