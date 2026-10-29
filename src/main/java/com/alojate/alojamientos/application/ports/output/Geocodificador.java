package com.alojate.alojamientos.application.ports.output;

import java.math.BigDecimal;

public interface Geocodificador {

    LugarGeocodificado porDireccion(String direccion, String ciudad, String departamento);

    LugarGeocodificado porCoordenadas(BigDecimal latitud, BigDecimal longitud);
}
