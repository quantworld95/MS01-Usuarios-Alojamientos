package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record GuardarPoliticasCommand(
        UUID anfitrionId,
        UUID alojamientoId,
        String politicas,
        String horarioEntrada,
        String horarioSalida) {
}
