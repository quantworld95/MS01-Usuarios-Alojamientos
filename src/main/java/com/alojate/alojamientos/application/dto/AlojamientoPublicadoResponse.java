package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record AlojamientoPublicadoResponse(
        UUID id,
        int tipoPropiedadId,
        String tipoPropiedad,
        String titulo,
        String ciudad,
        String departamento) {
}
