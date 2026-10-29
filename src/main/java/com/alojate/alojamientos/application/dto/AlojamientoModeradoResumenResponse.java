package com.alojate.alojamientos.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AlojamientoModeradoResumenResponse(
        UUID id,
        UUID anfitrionId,
        String nombreAnfitrion,
        int tipoPropiedadId,
        String tipoPropiedad,
        String titulo,
        String ciudad,
        String departamento,
        String estado,
        String motivo,
        Instant fechaCreacion) {
}
