package com.alojate.identidad.infrastructure.adapters.input.rest;

import java.util.List;

public record GuardarRolRequest(String nombre, String descripcion, List<Integer> permisos) {
}
