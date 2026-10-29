package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public record DescripcionUnidad(String valor) {

    public DescripcionUnidad {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La descripción de la habitación es obligatoria");
        }
        valor = valor.trim().replaceAll("\\s+", " ");
        if (valor.length() < 10 || valor.length() > 4000) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", "La descripción de la habitación admite entre 10 y 4000 caracteres");
        }
    }
}
