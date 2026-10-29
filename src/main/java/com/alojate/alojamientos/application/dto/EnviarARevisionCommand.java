package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record EnviarARevisionCommand(UUID anfitrionId, UUID alojamientoId) {
}
