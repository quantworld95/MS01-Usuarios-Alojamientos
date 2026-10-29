package com.alojate.identidad.domain.rol.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record DescripcionRol(String valor) {

    public DescripcionRol {
        if (valor == null || valor.isBlank()) {
            valor = null;
        } else {
            valor = valor.trim();
            if (valor.length() > 500) {
                throw new ReglaNegocioException("DATO_INVALIDO", "La descripción del rol admite hasta 500 caracteres");
            }
        }
    }
}
