package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

/**
 * Ciclo de publicación de una habitación (misma semántica que oferta casa/depto).
 */
public enum EstadoUnidad {
    BORRADOR,
    EN_REVISION,
    PUBLICADO,
    RECHAZADO,
    DESACTIVADO,
    SUSPENDIDO;

    public boolean admiteEdicionRegistro() {
        return this == BORRADOR || this == RECHAZADO;
    }

    public boolean esVisibleEnCatalogo() {
        return this == PUBLICADO;
    }

    /** Compat: antigua ACTIVA ≡ lista para ofrecer / publicada. */
    public boolean cuentaComoOfertaLista() {
        return this == PUBLICADO || this == EN_REVISION || this == BORRADOR;
    }

    public static EstadoUnidad desde(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El estado de la unidad es obligatorio");
        }
        String valor = texto.trim().toUpperCase();
        return switch (valor) {
            case "ACTIVA" -> PUBLICADO; // migración legacy
            case "INACTIVA", "PAUSADO" -> DESACTIVADO;
            default -> {
                try {
                    yield EstadoUnidad.valueOf(valor);
                } catch (IllegalArgumentException excepcion) {
                    throw new ReglaNegocioException(
                            "DATO_INVALIDO",
                            "El estado de la unidad debe ser BORRADOR, EN_REVISION, PUBLICADO, RECHAZADO, DESACTIVADO o SUSPENDIDO");
                }
            }
        };
    }
}
