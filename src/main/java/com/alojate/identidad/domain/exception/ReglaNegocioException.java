package com.alojate.identidad.domain.exception;

public final class ReglaNegocioException extends RuntimeException {

    private final String codigo;

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
