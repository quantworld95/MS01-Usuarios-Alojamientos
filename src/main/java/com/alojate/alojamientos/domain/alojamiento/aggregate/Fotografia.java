package com.alojate.alojamientos.domain.alojamiento.aggregate;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.util.UUID;

public final class Fotografia {

    private final UUID id;
    private final String identificadorAlmacen;
    private final String url;
    private final int orden;

    private Fotografia(UUID id, String identificadorAlmacen, String url, int orden) {
        this.id = id;
        this.identificadorAlmacen = validarIdentificador(identificadorAlmacen);
        this.url = validarUrl(url);
        this.orden = validarOrden(orden);
    }

    public static Fotografia nueva(String identificadorAlmacen, String url, int orden) {
        return new Fotografia(UUID.randomUUID(), identificadorAlmacen, url, orden);
    }

    public static Fotografia reconstituir(UUID id, String identificadorAlmacen, String url, int orden) {
        if (id == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La fotografía es obligatoria");
        }
        return new Fotografia(id, identificadorAlmacen, url, orden);
    }

    public UUID id() {
        return id;
    }

    public String identificadorAlmacen() {
        return identificadorAlmacen;
    }

    public String url() {
        return url;
    }

    public int orden() {
        return orden;
    }

    private static String validarIdentificador(String identificador) {
        if (identificador == null || identificador.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La fotografía no tiene archivo");
        }
        String limpio = identificador.trim();
        if (limpio.length() > 255 || !limpio.matches("[A-Za-z0-9_./-]+")) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El archivo de la fotografía no es válido");
        }
        return limpio;
    }

    private static String validarUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La fotografía es obligatoria");
        }
        String limpia = url.trim();
        if (limpia.length() > 500 || !(limpia.startsWith("https://") || limpia.startsWith("http://"))) {
            throw new ReglaNegocioException("DATO_INVALIDO", "La fotografía debe ser una dirección web");
        }
        return limpia;
    }

    private static int validarOrden(int orden) {
        if (orden < 1) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El orden de la fotografía empieza en 1");
        }
        return orden;
    }
}
