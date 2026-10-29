package com.alojate.alojamientos.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record GuardarOfertaUnicaCommand(
        UUID anfitrionId,
        UUID alojamientoId,
        Integer capacidadHuespedes,
        Integer cantidadDormitorios,
        Integer cantidadCamas,
        BigDecimal cantidadBanos,
        BigDecimal precioNoche,
        String politicas,
        String horarioEntrada,
        String horarioSalida) {
}
