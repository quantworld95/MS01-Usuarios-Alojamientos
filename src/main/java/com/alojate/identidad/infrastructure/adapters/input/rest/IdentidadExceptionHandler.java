package com.alojate.identidad.infrastructure.adapters.input.rest;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class IdentidadExceptionHandler {

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, String>> manejar(ReglaNegocioException excepcion) {
        return ResponseEntity.status(estado(excepcion.codigo()))
                .body(Map.of("codigo", excepcion.codigo(), "mensaje", excepcion.getMessage()));
    }

    private HttpStatus estado(String codigo) {
        return switch (codigo) {
            case "CUENTA_NO_ENCONTRADA", "ROL_NO_ENCONTRADO", "RESTRICCION_NO_ENCONTRADA" -> HttpStatus.NOT_FOUND;
            case "CREDENCIALES_INVALIDAS", "SESION_INVALIDA", "SESION_REQUERIDA" -> HttpStatus.UNAUTHORIZED;
            case "CUENTA_SUSPENDIDA", "CUENTA_DESACTIVADA", "CUENTA_CORPORATIVA", "CUENTA_PUBLICA",
                    "PERMISO_DENEGADO" -> HttpStatus.FORBIDDEN;
            case "CORREO_REGISTRADO", "CORREO_CORPORATIVO_REGISTRADO", "CUENTA_NO_PENDIENTE", "CUENTA_PENDIENTE",
                    "ROL_NO_ASIGNABLE", "ROL_INACTIVO", "ROL_PROTEGIDO", "ROL_DUPLICADO",
                    "CUENTA_NO_ACTIVABLE", "PERMISO_NO_RESTRINGIBLE", "RESTRICCION_VIGENTE",
                    "RESTRICCION_NO_VIGENTE" -> HttpStatus.CONFLICT;
            case "CORREO_NO_ENVIADO" -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_REQUEST;
        };
    }
}
