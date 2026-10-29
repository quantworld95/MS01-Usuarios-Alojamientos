package com.alojate.alojamientos.domain.alojamiento.politica;

import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoPublicacion;
import com.alojate.alojamientos.domain.alojamiento.valueobject.EstadoUnidad;
import com.alojate.alojamientos.domain.exception.ReglaNegocioException;

/**
 * Transiciones de habitación (diagrama UML) y cascada desde el establecimiento.
 */
public final class PoliticaEstadosHabitacion {

    public static final String MOTIVO_ESTABLECIMIENTO = "ESTABLECIMIENTO";

    private PoliticaEstadosHabitacion() {
    }

    public static EstadoUnidad trasEnviarARevision(EstadoUnidad actual) {
        if (actual != EstadoUnidad.BORRADOR && actual != EstadoUnidad.RECHAZADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "Solo una habitación en borrador o rechazada puede enviarse a revisión");
        }
        return EstadoUnidad.EN_REVISION;
    }

    public static EstadoUnidad trasAprobar(EstadoUnidad actual) {
        if (actual != EstadoUnidad.EN_REVISION) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se aprueba una habitación en revisión");
        }
        return EstadoUnidad.PUBLICADO;
    }

    public static EstadoUnidad trasRechazar(EstadoUnidad actual) {
        if (actual != EstadoUnidad.EN_REVISION) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se rechaza una habitación en revisión");
        }
        return EstadoUnidad.RECHAZADO;
    }

    public static EstadoUnidad trasDesactivarPorAnfitrion(EstadoUnidad actual) {
        if (actual != EstadoUnidad.PUBLICADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se desactiva una habitación publicada");
        }
        return EstadoUnidad.DESACTIVADO;
    }

    public static EstadoUnidad trasSuspender(EstadoUnidad actual) {
        if (actual != EstadoUnidad.PUBLICADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se suspende una habitación publicada");
        }
        return EstadoUnidad.SUSPENDIDO;
    }

    public static EstadoUnidad trasReactivarPorAnfitrion(
            EstadoUnidad actual, EstadoPublicacion estadoEstablecimiento) {
        if (actual != EstadoUnidad.DESACTIVADO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se reactiva una habitación desactivada");
        }
        exigirEstablecimientoActivo(estadoEstablecimiento);
        return EstadoUnidad.PUBLICADO;
    }

    public static EstadoUnidad trasLevantarSuspension(
            EstadoUnidad actual, EstadoPublicacion estadoEstablecimiento) {
        if (actual != EstadoUnidad.SUSPENDIDO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA", "Solo se levanta suspensión de una habitación suspendida");
        }
        exigirEstablecimientoActivo(estadoEstablecimiento);
        return EstadoUnidad.PUBLICADO;
    }

    public static EstadoUnidad cascadaDesdeEstablecimiento(
            EstadoUnidad actual, EstadoPublicacion nuevoEstadoEstablecimiento) {
        if (actual != EstadoUnidad.PUBLICADO) {
            return actual;
        }
        if (nuevoEstadoEstablecimiento == EstadoPublicacion.DESACTIVADO) {
            return EstadoUnidad.DESACTIVADO;
        }
        if (nuevoEstadoEstablecimiento == EstadoPublicacion.SUSPENDIDO) {
            return EstadoUnidad.SUSPENDIDO;
        }
        return actual;
    }

    public static EstadoUnidad restaurarTrasEstablecimientoActivo(
            EstadoUnidad actual, String ultimoMotivo) {
        if (!MOTIVO_ESTABLECIMIENTO.equals(ultimoMotivo)) {
            return actual;
        }
        if (actual == EstadoUnidad.DESACTIVADO || actual == EstadoUnidad.SUSPENDIDO) {
            return EstadoUnidad.PUBLICADO;
        }
        return actual;
    }

    private static void exigirEstablecimientoActivo(EstadoPublicacion estadoEstablecimiento) {
        if (estadoEstablecimiento != EstadoPublicacion.ACTIVO) {
            throw new ReglaNegocioException(
                    "PUBLICACION_NO_PERMITIDA",
                    "El establecimiento debe estar activado para publicar la habitación");
        }
    }
}
