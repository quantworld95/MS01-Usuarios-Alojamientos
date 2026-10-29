package com.alojate.identidad.application.dto;

import java.util.UUID;

public record ModificarTrabajadorCommand(
        UUID id,
        String nombre,
        String apellido,
        String email,
        String telefono,
        int idRol,
        Boolean activo) {
}
