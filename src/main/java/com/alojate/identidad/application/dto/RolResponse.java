package com.alojate.identidad.application.dto;

import java.util.List;

public record RolResponse(
        int id,
        String nombre,
        String descripcion,
        String ambito,
        boolean esSistema,
        boolean activo,
        List<Integer> permisos) {
}
