package com.alojate.identidad.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.UUID;

public record SesionResponse(
        String token,
        UUID usuarioId,
        String estado,
        String contexto,
        @JsonInclude(JsonInclude.Include.NON_NULL) List<String> permisos) {
}
