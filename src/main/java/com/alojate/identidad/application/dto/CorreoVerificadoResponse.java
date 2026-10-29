package com.alojate.identidad.application.dto;

import java.util.UUID;

public record CorreoVerificadoResponse(UUID usuarioId, String estado) {
}
