package com.alojate.identidad.domain.usuario.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record Telefono(String valor) {

    public Telefono {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El teléfono es obligatorio");
        }
        String normalizado = valor.trim();
        if (normalizado.length() > 30 || !normalizado.matches("^[0-9+()\\-\\s]+$")) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El teléfono no es válido");
        }
        valor = normalizado;
    }
}
