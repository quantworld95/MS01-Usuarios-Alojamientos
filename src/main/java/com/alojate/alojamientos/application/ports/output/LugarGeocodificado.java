package com.alojate.alojamientos.application.ports.output;

import java.math.BigDecimal;

public record LugarGeocodificado(
        String direccion,
        String ciudad,
        String departamento,
        BigDecimal latitud,
        BigDecimal longitud) {
}
