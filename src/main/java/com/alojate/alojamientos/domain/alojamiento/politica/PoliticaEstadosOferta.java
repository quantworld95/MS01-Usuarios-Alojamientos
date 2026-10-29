package com.alojate.alojamientos.domain.alojamiento.politica;

import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

/**
 * Transiciones de casa/departamento (ciclo de publicación del alojamiento).
 */
public final class PoliticaEstadosOferta {

    private PoliticaEstadosOferta() {
    }

    public static EstadoPublicacion trasEnviarARevision(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.BORRADOR && actual != EstadoPublicacion.RECHAZADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "Solo un borrador o un rechazo puede enviarse a revisión");
        }
        return EstadoPublicacion.EN_REVISION;
    }

    public static EstadoPublicacion trasAprobar(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.EN_REVISION) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se aprueba un alojamiento en revisión");
        }
        return EstadoPublicacion.PUBLICADO;
    }

    public static EstadoPublicacion trasRechazar(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.EN_REVISION) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se rechaza un alojamiento en revisión");
        }
        return EstadoPublicacion.RECHAZADO;
    }

    public static EstadoPublicacion trasDesactivar(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.PUBLICADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se desactiva un alojamiento publicado");
        }
        return EstadoPublicacion.DESACTIVADO;
    }

    public static EstadoPublicacion trasActivar(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.DESACTIVADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se activa un alojamiento desactivado");
        }
        return EstadoPublicacion.PUBLICADO;
    }

    public static EstadoPublicacion trasSuspender(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.PUBLICADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se suspende un alojamiento publicado");
        }
        return EstadoPublicacion.SUSPENDIDO;
    }

    public static EstadoPublicacion trasLevantarSuspension(EstadoPublicacion actual) {
        if (actual != EstadoPublicacion.SUSPENDIDO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se levanta la suspensión de un alojamiento suspendido");
        }
        return EstadoPublicacion.PUBLICADO;
    }
}
