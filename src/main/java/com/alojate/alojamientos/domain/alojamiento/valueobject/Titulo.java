package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public record Titulo(String valor) {

    public Titulo {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El título es obligatorio");
        }
        valor = valor.trim().replaceAll("\\s+", " ");
        if (valor.length() < 3 || valor.length() > 140) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El título admite entre 3 y 140 caracteres");
        }
    }
}
