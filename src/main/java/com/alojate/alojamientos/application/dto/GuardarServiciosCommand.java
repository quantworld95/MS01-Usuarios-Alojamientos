package com.alojate.alojamientos.application.dto;

import java.util.List;
import java.util.UUID;

public record GuardarServiciosCommand(UUID anfitrionId, UUID alojamientoId, List<Integer> servicios) {
}
