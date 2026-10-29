package com.alojate.identidad.application.dto;

import java.util.List;

public record ModificarRolCommand(int id, String nombre, String descripcion, List<Integer> permisos) {
}
