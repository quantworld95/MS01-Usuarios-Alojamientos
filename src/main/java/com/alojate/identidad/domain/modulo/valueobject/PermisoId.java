package com.alojate.identidad.domain.modulo.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record PermisoId(int valor) {

    public PermisoId {
        if (valor <= 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El identificador del permiso es obligatorio");
        }
    }
}
