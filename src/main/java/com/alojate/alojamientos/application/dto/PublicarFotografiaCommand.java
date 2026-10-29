package com.alojate.alojamientos.application.dto;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.util.UUID;

public record PublicarFotografiaCommand(
        UUID anfitrionId, UUID alojamientoId, byte[] contenido, String tipoContenido) {

    private static final int MAXIMO_BYTES = 5 * 1024 * 1024;

    public PublicarFotografiaCommand {
        if (contenido == null || contenido.length == 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La imagen es obligatoria");
        }
        if (contenido.length > MAXIMO_BYTES) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La imagen admite hasta 5 MB");
        }
        String tipo = tipoContenido == null ? "" : tipoContenido.toLowerCase();
        // Algunos navegadores mandan image/jpg en lugar de image/jpeg.
        if (tipo.equals("image/jpg")) {
            tipo = "image/jpeg";
        }
        if (!tipo.equals("image/jpeg") && !tipo.equals("image/png") && !tipo.equals("image/webp")) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La imagen debe ser JPG, PNG o WEBP");
        }
        tipoContenido = tipo;
    }
}
