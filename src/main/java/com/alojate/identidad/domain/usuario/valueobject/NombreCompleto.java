package com.alojate.identidad.domain.usuario.valueobject;

import com.alojate.identidad.domain.exception.ReglaNegocioException;

public record NombreCompleto(String nombre, String apellido) {

    public NombreCompleto {
        nombre = requerido(nombre, "El nombre es obligatorio");
        apellido = requerido(apellido, "El apellido es obligatorio");
    }

    private static String requerido(String valor, String mensaje) {
        if (valor == null || valor.isBlank() || valor.trim().length() > 120) {
            throw new ReglaNegocioException("DATO_INVALIDO", mensaje);
        }
        return valor.trim();
    }
}
