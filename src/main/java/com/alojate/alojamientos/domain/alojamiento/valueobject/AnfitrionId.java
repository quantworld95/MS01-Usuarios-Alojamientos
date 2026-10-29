package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.util.UUID;

public record AnfitrionId(UUID valor) {

    public AnfitrionId {
        if (valor == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El anfitrión es obligatorio");
        }
    }
}
