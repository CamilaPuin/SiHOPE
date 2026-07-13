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
@Table(name = "historial")
public class History {

    public static final String CAMBIO_ROL = "CAMBIO_ROL";
    public static final String ESTADO_CUENTA = "ESTADO_CUENTA";
    public static final String SEGURIDAD = "SEGURIDAD";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    @JsonProperty("usuario")
    private User user;

    @Column(name = "tipo")
    @JsonProperty("tipo")
    private String type;

    @Column(name = "descripcion")
    @JsonProperty("descripcion")
    private String description;

    @Column(name = "fecha")
    @JsonProperty("fecha")
    private LocalDateTime date;

    public History() {
    }

    public History(User user, String type, String description, LocalDateTime date) {
        this.user = user;
        this.type = type;
        this.description = description;
        this.date = date;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public User getUsuario() {
        return getUser();
    }

    public void setUsuario(User usuario) {
        setUser(usuario);
    }

    public String getTipo() {
        return getType();
    }

    public void setTipo(String tipo) {
        setType(tipo);
    }

    public String getDescripcion() {
        return getDescription();
    }

    public void setDescripcion(String descripcion) {
        setDescription(descripcion);
    }

    public LocalDateTime getFecha() {
        return getDate();
    }

    public void setFecha(LocalDateTime fecha) {
        setDate(fecha);
    }
}
