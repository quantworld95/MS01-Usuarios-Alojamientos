package com.alojate.alojamientos.infrastructure.adapters.input.rest;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class AlojamientosExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> manejar(ReglaNegocioException excepcion) {
        return ResponseEntity.status(estado(excepcion.codigo()))
                .body(Map.of("codigo", excepcion.codigo(), "mensaje", excepcion.getMessage()));
    }

    private HttpStatus estado(String codigo) {
        return switch (codigo) {
            case "ALOJAMIENTO_NO_ENCONTRADO", "UNIDAD_NO_ENCONTRADA", "CUENTA_NO_ENCONTRADA" -> HttpStatus.NOT_FOUND;
            case "PERMISO_DENEGADO" -> HttpStatus.FORBIDDEN;
            case "SESION_INVALIDA", "SESION_REQUERIDA" -> HttpStatus.UNAUTHORIZED;
            case "PUBLICACION_NO_PERMITIDA" -> HttpStatus.CONFLICT;
            case "ALMACEN_NO_DISPONIBLE", "MAPA_NO_DISPONIBLE" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
