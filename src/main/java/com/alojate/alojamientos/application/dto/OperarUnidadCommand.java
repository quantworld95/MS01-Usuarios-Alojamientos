package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record OperarUnidadCommand(UUID anfitrionId, UUID alojamientoId, UUID unidadId) {
}
