package com.alojate.identidad.application.dto;

public record RegistrarTrabajadorCommand(
        String nombre,
        String apellido,
        String email,
        String telefono,
        String contrasena,
        int idRol) {
}
