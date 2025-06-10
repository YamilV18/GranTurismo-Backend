package org.example.granturismo.excepciones;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones personalizadas del sistema
     */
    @ExceptionHandler(ExcepcionPersonalizada.class)
    public ResponseEntity<ErrorResponse> handleExcepcionPersonalizada(ExcepcionPersonalizada e) {
        log.error("Excepción personalizada: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de negocio")
                .message(e.getMessage())
                .path("/api/preferences")
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja errores de validación de datos de entrada
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException e) {
        log.error("Error de validación: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Error de validación")
                .message("Los datos de entrada no son válidos")
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Maneja errores de conexión con APIs externas
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessException(ResourceAccessException e) {
        log.error("Error de acceso a recurso externo: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.SERVICE_UNAVAILABLE.value())
                .error("Servicio no disponible")
                .message("No se pudo conectar con el servicio externo. Por favor, intente más tarde.")
                .build();

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }

    /**
     * Maneja errores generales de cliente REST
     */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClientException(RestClientException e) {
        log.error("Error en cliente REST: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_GATEWAY.value())
                .error("Error de gateway")
                .message("Error al comunicarse con servicios externos")
                .build();

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }

    /**
     * Maneja errores de autenticación/autorización
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(SecurityException e) {
        log.error("Error de seguridad: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .error("Error de autenticación")
                .message("Usuario no autenticado o sin permisos suficientes")
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Maneja errores cuando no se encuentra un recurso
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("Error en tiempo de ejecución: {}", e.getMessage(), e);

        // Determinar el tipo de error basado en el mensaje
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorType = "Error interno";

        if (e.getMessage().contains("no encontrado") || e.getMessage().contains("not found")) {
            status = HttpStatus.NOT_FOUND;
            errorType = "Recurso no encontrado";
        } else if (e.getMessage().contains("no autenticado") || e.getMessage().contains("unauthorized")) {
            status = HttpStatus.UNAUTHORIZED;
            errorType = "Error de autenticación";
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(errorType)
                .message(e.getMessage())
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Maneja cualquier excepción no capturada
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        log.error("Error general no manejado: {}", e.getMessage(), e);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Error interno del servidor")
                .message("Ha ocurrido un error inesperado. Por favor, contacte al administrador.")
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Maneja errores específicos de traducción
     */
    @ExceptionHandler(TranslationException.class)
    public ResponseEntity<ErrorResponse> handleTranslationException(TranslationException e) {
        log.warn("Error de traducción: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PARTIAL_CONTENT.value())
                .error("Error de traducción")
                .message("No se pudo traducir el contenido. Se muestra en idioma original.")
                .build();

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(errorResponse);
    }

    /**
     * Maneja errores específicos de conversión de moneda
     */
    @ExceptionHandler(CurrencyConversionException.class)
    public ResponseEntity<ErrorResponse> handleCurrencyConversionException(CurrencyConversionException e) {
        log.warn("Error de conversión de moneda: {}", e.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PARTIAL_CONTENT.value())
                .error("Error de conversión de moneda")
                .message("No se pudo convertir el precio. Se muestra en moneda original.")
                .build();

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(errorResponse);
    }
}