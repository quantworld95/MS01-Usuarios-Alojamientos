package com.alojate.alojamientos.domain.catalogo.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public record ServicioId(int valor) {

    public ServicioId {
        if (valor <= 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El servicio es obligatorio");
        }
    }
}
