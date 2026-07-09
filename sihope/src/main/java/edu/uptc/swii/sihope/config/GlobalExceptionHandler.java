package edu.uptc.swii.sihope.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import edu.uptc.swii.sihope.dto.response.ApiResponse;

/**
 * Manejo centralizado de errores para toda la API REST. Traduce las excepciones
 * a respuestas JSON con el envoltorio {@link ApiResponse} (nunca redirects ni vistas).
 * La validación de negocio (mapas de error de UserService) la manejan los propios
 * controladores; aquí se cubren fallos de la petición y errores inesperados.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Cuerpo JSON ausente o mal formado. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> manejarJsonInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("El cuerpo de la petición es inválido o está mal formado."));
    }

    /** Parámetro de ruta o query con tipo incorrecto (p. ej. id no numérico). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> manejarTipoInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("El parámetro '" + ex.getName() + "' tiene un valor inválido."));
    }

    /** Cualquier error no controlado. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> manejarErrorInterno(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Ocurrió un error interno. Intenta nuevamente más tarde."));
    }

    /** Recurso estático no encontrado (p. ej. GET / sin index.html). */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> manejarRecursoNoEncontrado(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Recurso no encontrado."));
    }
}
