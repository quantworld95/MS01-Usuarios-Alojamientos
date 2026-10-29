package com.alojate.identidad.domain.usuario.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record Contrasena(String valor) {

    public Contrasena {
        if (valor == null || valor.length() < 8) {
            throw new ReglaNegocioException(
                    "CONTRASENA_INVALIDA", "La contraseña debe tener al menos 8 caracteres");
        }
    }
}
