package com.alojate.alojamientos.domain.alojamiento.valueobject;

import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

import java.math.BigDecimal;

public record PrecioNoche(BigDecimal valor) {

    private static final BigDecimal MAXIMO = new BigDecimal("1000000");

    public PrecioNoche {
        if (valor == null || valor.scale() > 2 || valor.signum() <= 0 || valor.compareTo(MAXIMO) >= 0) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El precio por noche debe ser mayor a cero");
        }
    }
}
