package com.alojate.identidad.application.dto;

import java.util.UUID;

public record TrabajadorRegistradoResponse(UUID usuarioId, String estado, int rolId) {
}
