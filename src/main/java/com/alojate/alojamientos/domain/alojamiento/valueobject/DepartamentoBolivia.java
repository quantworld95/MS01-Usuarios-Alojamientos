package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;

public final class DepartamentoBolivia {

    private static final Map<String, String> NOMBRES = Map.of(
            "chuquisaca", "Chuquisaca",
            "la paz", "La Paz",
            "cochabamba", "Cochabamba",
            "oruro", "Oruro",
            "potosi", "Potosí",
            "tarija", "Tarija",
            "santa cruz", "Santa Cruz",
            "beni", "Beni",
            "pando", "Pando");

    private DepartamentoBolivia() {
    }

    public static String canonico(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El departamento es obligatorio");
        }
        String plano = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
        String oficial = NOMBRES.get(plano);
        if (oficial == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El departamento no pertenece a Bolivia");
        }
        return oficial;
    }
}
