package com.alojate.identidad.domain.usuario.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record Email(String valor) {

    public Email {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("CORREO_INVALIDO", "El correo es obligatorio");
        }
        String normalizado = valor.trim().toLowerCase();
        if (normalizado.length() > 180 || !normalizado.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new ReglaNegocioException("CORREO_INVALIDO", "El correo no es válido");
        }
        valor = normalizado;
    }
}
