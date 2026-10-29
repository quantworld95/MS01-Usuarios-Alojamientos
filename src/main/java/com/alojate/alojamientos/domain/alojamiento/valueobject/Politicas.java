package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public record Politicas(String valor) {

    public Politicas {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "Las políticas son obligatorias");
        }
        valor = valor.trim();
        if (valor.length() < 10 || valor.length() > 4000) {
            throw new ReglaNegocioException("DATO_INVALIDO", "Las políticas admiten entre 10 y 4000 caracteres");
        }
    }
}
