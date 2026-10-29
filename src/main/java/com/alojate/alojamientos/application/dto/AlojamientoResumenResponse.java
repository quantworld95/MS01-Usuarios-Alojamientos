package com.alojate.alojamientos.application.dto;

import java.time.Instant;
import java.util.UUID;

public record AlojamientoResumenResponse(
        UUID id,
        int tipoPropiedadId,
        String tipoPropiedad,
        String titulo,
        String ciudad,
        String departamento,
        String estado,
        String motivo,
        Instant fechaCreacion,
        boolean establecimientoCompleto,
        boolean listoParaRevision,
        String fotoPortada) {
}
