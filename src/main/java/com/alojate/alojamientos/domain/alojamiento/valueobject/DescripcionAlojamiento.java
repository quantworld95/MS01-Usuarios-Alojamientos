package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public record DescripcionAlojamiento(String valor) {

    public DescripcionAlojamiento {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La descripción es obligatoria");
        }
        valor = valor.trim();
        if (valor.length() < 10 || valor.length() > 4000) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La descripción admite entre 10 y 4000 caracteres");
        }
    }
}
