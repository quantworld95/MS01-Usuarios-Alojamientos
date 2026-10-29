package com.alojate.identidad.domain.modulo.aggregate;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.valueobject.ModuloId;

import java.util.List;

public final class Modulo {

    private final ModuloId id;
    private final String nombre;
    private final String descripcion;
    private final int orden;
    private final List<Permiso> permisos;

    private Modulo(ModuloId id, String nombre, String descripcion, int orden, List<Permiso> permisos) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.orden = orden;
        this.permisos = permisos;
    }

    public static Modulo reconstituir(
            ModuloId id, String nombre, String descripcion, int orden, List<Permiso> permisos) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 120) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El nombre del módulo es obligatorio");
        }
        String texto = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (texto != null && texto.length() > 500) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La descripción del módulo admite hasta 500 caracteres");
        }
        return new Modulo(id, nombre.trim(), texto, orden, List.copyOf(permisos));
    }

    public ModuloId id() {
        return id;
    }

    public String nombre() {
        return nombre;
    }

    public String descripcion() {
        return descripcion;
    }

    public int orden() {
        return orden;
    }

    public List<Permiso> permisos() {
        return permisos;
    }
}
