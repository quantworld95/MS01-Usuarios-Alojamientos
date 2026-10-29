package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public record HorarioEstadia(String valor) {

    public HorarioEstadia {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El horario es obligatorio");
        }
        valor = valor.trim();
        if (!valor.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El horario debe tener formato HH:mm");
        }
    }
}
