package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record FinalizarRegistroCommand(UUID anfitrionId, UUID alojamientoId) {
}
