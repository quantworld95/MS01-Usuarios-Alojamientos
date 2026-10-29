package com.alojate.identidad.domain.modulo.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record CodigoPermiso(String valor) {

    public CodigoPermiso {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El código del permiso es obligatorio");
        }
        valor = valor.trim();
        if (valor.length() > 80) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El código del permiso admite hasta 80 caracteres");
        }
    }
}
