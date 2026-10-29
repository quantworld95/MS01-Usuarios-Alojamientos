package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record DecidirUnidadCommand(
        UUID administradorId, UUID alojamientoId, UUID unidadId, String motivo) {
}
