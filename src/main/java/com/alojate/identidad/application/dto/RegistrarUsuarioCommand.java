package com.alojate.identidad.application.dto;

public record RegistrarUsuarioCommand(
        String nombre,
        String apellido,
        String email,
        String telefono,
        String contrasena) {
}
