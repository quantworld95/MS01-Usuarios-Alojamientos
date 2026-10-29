package com.alojate.identidad.application.dto;

import java.util.List;

public record ModuloResponse(int id, String nombre, String descripcion, int orden, List<PermisoResponse> permisos) {
}
