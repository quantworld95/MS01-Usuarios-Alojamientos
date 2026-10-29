package com.alojate.identidad.domain.rol.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record NombreRol(String valor) {

    public static final String USUARIO_PUBLICO = "USUARIO_PUBLICO";

    public NombreRol {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El nombre del rol es obligatorio");
        }
        valor = valor.trim();
        if (valor.length() > 120) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El nombre del rol admite hasta 120 caracteres");
        }
    }

    public boolean esUsuarioPublico() {
        return USUARIO_PUBLICO.equals(valor);
    }
}
