package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record DecidirPublicacionCommand(UUID administradorId, UUID alojamientoId, String motivo) {
}
