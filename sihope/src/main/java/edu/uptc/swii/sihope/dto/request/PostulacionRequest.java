package edu.uptc.swii.sihope.dto.request;

import java.util.Map;

/**
 * Formulario de postulación del aspirante (HU_005). Los campos son parametrizables:
 * el frontend decide qué solicitar y los envía como pares clave→valor, que el
 * backend serializa a JSON sin acoplarse a un conjunto fijo de campos.
 */
public class PostulacionRequest {

    private Map<String, String> datos;

    public Map<String, String> getDatos() {
        return datos;
    }

    public void setDatos(Map<String, String> datos) {
        this.datos = datos;
    }
}
