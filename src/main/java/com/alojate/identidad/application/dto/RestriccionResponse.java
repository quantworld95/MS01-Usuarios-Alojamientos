package com.alojate.identidad.application.dto;

import java.time.Instant;
import java.util.UUID;

public record RestriccionResponse(
        UUID id,
        UUID usuarioId,
        int permisoId,
        UUID administradorId,
        String motivo,
        Instant fechaInicio,
        Instant fechaFin,
        boolean activa) {
}
