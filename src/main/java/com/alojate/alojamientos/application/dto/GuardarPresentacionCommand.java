package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record GuardarPresentacionCommand(
        UUID anfitrionId,
        UUID alojamientoId,
        String titulo,
        String descripcion,
        String politicas,
        String horarioEntrada,
        String horarioSalida) {
}
