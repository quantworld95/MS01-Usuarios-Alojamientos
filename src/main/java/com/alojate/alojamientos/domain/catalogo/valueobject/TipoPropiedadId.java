package com.alojate.alojamientos.domain.catalogo.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public record TipoPropiedadId(int valor) {

    public TipoPropiedadId {
        if (valor <= 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El tipo de propiedad es obligatorio");
        }
    }
}
