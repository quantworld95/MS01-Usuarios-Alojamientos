package com.alojate.alojamientos.application.services.registro;

import com.alojate.alojamientos.application.dto.GuardarUbicacionCommand;
import com.alojate.alojamientos.application.ports.output.Geocodificador;
import com.alojate.alojamientos.application.ports.output.LugarGeocodificado;
import com.alojate.alojamientos.domain.alojamiento.valueobject.Ubicacion;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

public final class CompletarUbicacion {

    private final Geocodificador geocodificador;

    public CompletarUbicacion(Geocodificador geocodificador) {
        this.geocodificador = geocodificador;
    }

    public Ubicacion completar(GuardarUbicacionCommand comando) {
        if (comando.latitud() == null ^ comando.longitud() == null) {
            throw new ReglaNegocioException("DATO_INVALIDO", "El punto en el mapa está incompleto");
        }
        boolean direccion = presente(comando.direccion());
        boolean gps = comando.latitud() != null;
        if (direccion && gps) {
            return new Ubicacion(
                    comando.direccion(),
                    comando.ciudad(),
                    comando.departamento(),
                    comando.latitud(),
                    comando.longitud());
        }
        if (direccion) {
            LugarGeocodificado lugar = geocodificador.porDireccion(
                    comando.direccion(), comando.ciudad(), comando.departamento());
            return new Ubicacion(
                    comando.direccion(),
                    primero(comando.ciudad(), lugar.ciudad()),
                    primero(comando.departamento(), lugar.departamento()),
                    lugar.latitud(),
                    lugar.longitud());
        }
        if (gps) {
            LugarGeocodificado lugar = geocodificador.porCoordenadas(comando.latitud(), comando.longitud());
            return new Ubicacion(
                    lugar.direccion(),
                    lugar.ciudad(),
                    lugar.departamento(),
                    comando.latitud(),
                    comando.longitud());
        }
        throw new ReglaNegocioException("DATO_INVALIDO", "La dirección o el punto en el mapa es obligatorio");
    }

    private static boolean presente(String valor) {
        return valor != null && !valor.isBlank();
    }

    private static String primero(String preferido, String alternativa) {
        return presente(preferido) ? preferido : alternativa;
    }
}
