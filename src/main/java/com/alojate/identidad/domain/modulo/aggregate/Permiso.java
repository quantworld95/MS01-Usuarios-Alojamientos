package com.alojate.identidad.domain.modulo.aggregate;

import com.alojate.identidad.domain.exception.ReglaNegocioException;
import com.alojate.identidad.domain.modulo.valueobject.CodigoPermiso;
import com.alojate.identidad.domain.modulo.valueobject.PermisoId;

public final class Permiso {

    private final PermisoId id;
    private final CodigoPermiso codigo;
    private final String nombre;
    private final String descripcion;
    private final boolean asignable;

    private Permiso(PermisoId id, CodigoPermiso codigo, String nombre, String descripcion, boolean asignable) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.asignable = asignable;
    }

    public static Permiso reconstituir(
            PermisoId id, CodigoPermiso codigo, String nombre, String descripcion, boolean asignable) {
        if (nombre == null || nombre.isBlank() || nombre.trim().length() > 120) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El nombre del permiso es obligatorio");
        }
        String texto = descripcion == null || descripcion.isBlank() ? null : descripcion.trim();
        if (texto != null && texto.length() > 500) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La descripción del permiso admite hasta 500 caracteres");
        }
        return new Permiso(id, codigo, nombre.trim(), texto, asignable);
    }

    public PermisoId id() {
        return id;
    }

    public CodigoPermiso codigo() {
        return codigo;
    }

    public String nombre() {
        return nombre;
    }

    public String descripcion() {
        return descripcion;
    }

    public boolean asignable() {
        return asignable;
    }
}
