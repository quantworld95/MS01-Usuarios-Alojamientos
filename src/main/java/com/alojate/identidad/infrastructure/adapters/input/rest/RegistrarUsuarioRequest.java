package com.alojate.identidad.infrastructure.adapters.input.rest;

public record RegistrarUsuarioRequest(
        String nombre,
        String apellido,
        String email,
        String telefono,
        String contrasena) {
}
