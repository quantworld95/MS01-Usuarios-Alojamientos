package com.alojate.alojamientos.domain.alojamiento.politica;

import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

/**
 * Transiciones operativas del hotel/hostal (Activado / Desactivado / Suspendido).
 */
public final class PoliticaEstadosEstablecimiento {

    private PoliticaEstadosEstablecimiento() {
    }

    public static EstadoPublicacion trasDesactivar(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.ACTIVO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se desactiva un establecimiento activado");
        }
        return EstadoPublicacion.DESACTIVADO;
    }

    public static EstadoPublicacion trasActivar(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.DESACTIVADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se activa un establecimiento desactivado");
        }
        return EstadoPublicacion.ACTIVO;
    }

    public static EstadoPublicacion trasSuspender(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.ACTIVO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se suspende un establecimiento activado");
        }
        return EstadoPublicacion.SUSPENDIDO;
    }

    public static EstadoPublicacion trasLevantarSuspension(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.SUSPENDIDO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "Solo se levanta la suspensión de un establecimiento suspendido");
        }
        return EstadoPublicacion.ACTIVO;
    }
}
