package com.alojate.identidad.application.dto;

import java.util.List;

public record CrearRolCommand(String nombre, String descripcion, List<Integer> permisos) {
}
