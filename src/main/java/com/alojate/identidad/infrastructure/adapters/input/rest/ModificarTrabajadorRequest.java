package com.alojate.identidad.infrastructure.adapters.input.rest;

public record ModificarTrabajadorRequest(
        String nombre,
        String apellido,
        String email,
        String telefono,
        int idRol,
        Boolean activo) {
}
