package com.alojate.identidad.application.dto;

import java.util.UUID;

public record CrearRestriccionCommand(UUID usuarioId, UUID administradorId, int idPermiso, String motivo) {
}
