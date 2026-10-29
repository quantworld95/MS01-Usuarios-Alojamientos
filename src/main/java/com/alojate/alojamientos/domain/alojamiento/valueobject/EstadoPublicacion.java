package com.alojate.alojamientos.domain.alojamiento.valueobject;

/**
 * Estado del alojamiento.
 * <ul>
 *   <li>Casa/departamento: ciclo de publicación (BORRADOR…PUBLICADO…DESACTIVADO/SUSPENDIDO).</li>
 *   <li>Hotel/hostal: operativo (BORRADOR wizard → ACTIVO ↔ DESACTIVADO / SUSPENDIDO).</li>
 * </ul>
 */
public enum EstadoPublicacion {
    BORRADOR,
    /** Hotel/hostal tras finalizar registro (Activado en el diagrama). */
    ACTIVO,
    EN_REVISION,
    PUBLICADO,
    RECHAZADO,
    /** Anfitrión desactiva (antes PAUSADO). */
    DESACTIVADO,
    SUSPENDIDO;

    /** Permite completar wizard / gestionar unidades según el tipo. */
    public boolean admiteRegistroParcial() {
        return this == BORRADOR || this == RECHAZADO || this == ACTIVO || this == DESACTIVADO;
    }

    public boolean esOperativoEstablecimiento() {
        return this == ACTIVO;
    }

    public static EstadoPublicacion desdeLegacy(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String valor = texto.trim().toUpperCase();
        if ("PAUSADO".equals(valor)) {
            return DESACTIVADO;
        }
        return EstadoPublicacion.valueOf(valor);
    }
}
