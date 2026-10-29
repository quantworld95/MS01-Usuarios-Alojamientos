package com.alojate.alojamientos.application.dto;

import java.util.UUID;

public record RegistrarAlojamientoCommand(UUID anfitrionId, Integer tipoPropiedadId) {
}
