package com.alojate.identidad.domain.usuario.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

import java.util.UUID;

public record UsuarioId(UUID valor) {

    public UsuarioId {
        if (valor == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El identificador del usuario es obligatorio");
        }
    }

    public static UsuarioId generar() {
        return new UsuarioId(UUID.randomUUID());
    }
}
