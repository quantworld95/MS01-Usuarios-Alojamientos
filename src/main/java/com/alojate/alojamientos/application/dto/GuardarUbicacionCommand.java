package com.alojate.alojamientos.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GuardarUbicacionCommand(
        UUID anfitrionId,
        UUID alojamientoId,
        String direccion,
        String ciudad,
        String departamento,
        BigDecimal latitud,
        BigDecimal longitud) {
}
