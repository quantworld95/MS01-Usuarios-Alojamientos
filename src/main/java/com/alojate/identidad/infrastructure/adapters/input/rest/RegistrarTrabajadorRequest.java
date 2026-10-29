package com.alojate.identidad.infrastructure.adapters.input.rest;

public record RegistrarTrabajadorRequest(
        String nombre,
        String apellido,
        String email,
        String telefono,
        String contrasena,
        int idRol) {
}
