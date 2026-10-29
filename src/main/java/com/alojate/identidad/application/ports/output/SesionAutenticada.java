package com.alojate.identidad.application.ports.output;

import java.util.List;
import java.util.UUID;

public record SesionAutenticada(UUID usuarioId, String contexto, List<String> permisos) {
}
