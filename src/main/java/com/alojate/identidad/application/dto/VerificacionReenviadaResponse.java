package com.alojate.identidad.application.dto;

import java.util.UUID;

public record VerificacionReenviadaResponse(UUID usuarioId, String estado) {
}
