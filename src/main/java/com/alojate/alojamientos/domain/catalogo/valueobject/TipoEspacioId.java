package com.alojate.alojamientos.domain.catalogo.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public record TipoEspacioId(int valor) {

    public TipoEspacioId {
        if (valor <= 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El tipo de espacio es obligatorio");
        }
    }
}
