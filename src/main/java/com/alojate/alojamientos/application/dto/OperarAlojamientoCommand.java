package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record OperarAlojamientoCommand(UUID anfitrionId, UUID alojamientoId) {
}
