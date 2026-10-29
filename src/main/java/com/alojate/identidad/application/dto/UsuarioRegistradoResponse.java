package com.alojate.identidad.application.dto;

import java.util.UUID;

public record UsuarioRegistradoResponse(UUID usuarioId, String estado) {
}
