package com.alojate.identidad.domain.modulo.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record ModuloId(int valor) {

    public ModuloId {
        if (valor <= 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El identificador del módulo es obligatorio");
        }
    }
}
