package com.alojate.identidad.domain.rol.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record RolId(int valor) {

    public RolId {
        if (valor <= 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El identificador del rol es obligatorio");
        }
    }
}
