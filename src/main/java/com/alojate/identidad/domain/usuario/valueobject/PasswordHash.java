package com.alojate.identidad.domain.usuario.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record PasswordHash(String valor) {

    public PasswordHash {
        if (valor == null || valor.isBlank() || valor.length() < 8 || valor.length() > 255) {
            throw new ReglaNegocioException("CONTRASENA_INVALIDA", "La contraseña cifrada no es válida");
        }
    }
}
