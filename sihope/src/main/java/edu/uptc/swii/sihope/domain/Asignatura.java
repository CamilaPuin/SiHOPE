package edu.uptc.swii.sihope.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "asignatura")
public class Asignatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "codigo")
    @JsonProperty("codigo")
    private String code;

    @Column(name = "nombre", nullable = false)
    @JsonProperty("nombre")
    private String name;

    public Asignatura() {
    }

    public Asignatura(String name) {
        this.name = name;
    }

    public Asignatura(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public String getCodigo() {
        return getCode();
    }

    public void setCodigo(String codigo) {
        setCode(codigo);
    }

    @JsonIgnore
    public String getNombre() {
        return getName();
    }

    public void setNombre(String nombre) {
        setName(nombre);
    }
}
