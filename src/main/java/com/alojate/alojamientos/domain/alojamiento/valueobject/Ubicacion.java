package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Ubicacion(
        String direccion,
        String ciudad,
        String departamento,
        BigDecimal latitud,
        BigDecimal longitud) {

    public Ubicacion {
        direccion = texto(direccion, 5, 200, "La dirección");
        ciudad = texto(ciudad, 2, 80, "La ciudad");
        departamento = DepartamentoBolivia.canonico(departamento);
        latitud = coordenada(latitud, new BigDecimal("-23.0"), new BigDecimal("-9.5"), "La latitud");
        longitud = coordenada(longitud, new BigDecimal("-69.6"), new BigDecimal("-57.5"), "La longitud");
    }

    private static String texto(String valor, int minimo, int maximo, String nombre) {
        if (valor == null || valor.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", nombre + " es obligatoria");
        }
        String limpio = valor.trim().replaceAll("\\s+", " ");
        if (limpio.length() < minimo || limpio.length() > maximo) {
            throw new ReglaNegocioException(
                    "DATO_INVALIDO", nombre + " admite entre " + minimo + " y " + maximo + " caracteres");
        }
        return limpio;
    }

    private static BigDecimal coordenada(BigDecimal valor, BigDecimal minimo, BigDecimal maximo, String nombre) {
        if (valor == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", nombre + " es obligatoria");
        }
        BigDecimal redondeada = valor.setScale(6, RoundingMode.HALF_UP);
        if (redondeada.compareTo(minimo) < 0 || redondeada.compareTo(maximo) > 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", nombre + " queda fuera de Bolivia");
        }
        return redondeada;
    }
}
