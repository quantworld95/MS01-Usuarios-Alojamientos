package com.alojate.identidad.application.dto;

import java.util.UUID;

public record CambiarEstadoUsuarioPublicoCommand(UUID id, String estado) {
}
