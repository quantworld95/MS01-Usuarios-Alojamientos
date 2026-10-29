package com.alojate.identidad.application.dto;

import java.util.UUID;

public record TrabajadorResponse(
        UUID id,
        String nombre,
        String apellido,
        String email,
        String telefono,
        String estado,
        int rolId) {
}
