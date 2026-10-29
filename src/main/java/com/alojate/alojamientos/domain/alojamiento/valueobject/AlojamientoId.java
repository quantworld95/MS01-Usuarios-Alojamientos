package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.util.UUID;

public record AlojamientoId(UUID valor) {

    public AlojamientoId {
        if (valor == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El alojamiento es obligatorio");
        }
    }

    public static AlojamientoId nuevo() {
        return new AlojamientoId(UUID.randomUUID());
    }
}
